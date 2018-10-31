/*
 * Copyright (C) 2018  Well-Factored Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package clovis.services

import cats.implicits._
import cats.{Monad, ~>}
import clovis.database.rows.{AccountRow, ActorType, RowId}
import clovis.database.{AccountDatabase, FollowCounts}
import clovis.entities.{Account, AccountId, Emoji, EntityId}

trait AccountService[F[_]] {
  def findAccount(id: AccountId): F[Option[Account]]
}

class AccountSvcImpl[F[_]: Monad, G[_]](accountDatabase: AccountDatabase[G])(implicit tx: G ~> F)
    extends AccountService[F] {

  def acct(row: AccountRow): String = {
    row.domain match {
      case None         => row.username
      case Some(domain) => s"${row.username}@$domain"
    }
  }

  def toRowId[T, T2](entityId: EntityId[T]): RowId[T2] = RowId[T2](entityId.id)
  def toEntityId[T, T2](rowId: RowId[T]): EntityId[T2] = EntityId[T2](rowId.id)

  override def findAccount(id: AccountId): F[Option[Account]] = {
    val F = Monad[F]

    val result: F[Option[(AccountRow, FollowCounts, Int)]] = tx(
      accountDatabase.accountWithFollows(toRowId(id)))

    result.flatMap {
      case Some((acc, fs, statusCount)) =>
        val moved: F[Option[Account]] = acc.movedToAccount match {
          case None            => F.pure(None)
          case Some(movedToId) => findAccount(toEntityId(movedToId))
        }

        moved.map { m =>
          Some(
            Account(
              EntityId[Account](acc.id.id),
              acc.username,
              acct(acc),
              acc.displayName,
              acc.locked,
              acc.createdAt,
              fs.followers,
              fs.following,
              statusCount,
              acc.note,
              acc.url,
              acc.avatar,
              acc.avatarStatic,
              acc.header,
              acc.headerStatic,
              List.empty[Emoji],
              m.map(_.acct),
              None,
              Some(acc.actorType == ActorType.Service)
            )
          )
        }

      case None => F.pure(None)
    }

  }
}
