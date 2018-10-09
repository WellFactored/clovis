package scaladon.services

import cats.Monad
import cats.data.Nested
import cats.implicits._
import scaladon.database.AccountDatabase
import scaladon.database.rows.{AccountRow, ActorType, RowId}
import scaladon.entities.{Account, AccountId, Emoji, EntityId}

trait AccountService[F[_]] {
  def findAccount(id: AccountId): F[Option[Account]]
}

class AccountSvcImpl[F[_] : Monad](accountDatabase: AccountDatabase[F]) extends AccountService[F] {

  def acct(row: AccountRow): String = {
    row.domain match {
      case None         => row.username
      case Some(domain) => s"${row.username}@${row.domain}"
    }
  }

  def toRowId[T, T2](entityId: EntityId[T]): RowId[T2] = RowId[T2](entityId.id)
  def toEntityId[T, T2](rowId: RowId[T]): EntityId[T2] = EntityId[T2](rowId.id)

  override def findAccount(id: AccountId): F[Option[Account]] = {
    // keep intellij happy
    type NestedRow[A] = Nested[F, Option, A]
    val F = Monad[F]

    accountDatabase.accountById(toRowId(id)).flatMap {
      case Some(row) =>
        val moved: F[Option[Account]] = row.movedToAccount match {
          case None            => F.pure(None)
          case Some(movedToId) => findAccount(toEntityId(movedToId))
        }

        moved.map { m =>
          Some(
            Account(
              EntityId[Account](row.id.id),
              row.username, acct(row),
              row.displayName,
              row.locked,
              row.createdAt,
              row.followersCount,
              row.followingCount,
              row.statusesCount,
              row.note,
              row.url,
              row.avatar,
              row.avatarStatic,
              row.header,
              row.headerStatic,
              List.empty[Emoji],
              m.map(_.acct),
              None,
              Some(row.actorType == ActorType.Service)
            )
          )
        }

      case None => F.pure(None)
    }

  }
}
