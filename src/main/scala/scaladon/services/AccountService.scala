package scaladon.services

import cats.data.Nested
import cats.implicits._
import cats.{Monad, ~>}
import scaladon.database.{AccountDatabase, FollowCounts}
import scaladon.database.rows.{AccountRow, ActorType, RowId}
import scaladon.entities.{Account, AccountId, Emoji, EntityId}

trait AccountService[F[_]] {
  def findAccount(id: AccountId): F[Option[Account]]
}

class AccountSvcImpl[F[_] : Monad, G[_]](accountDatabase: AccountDatabase[G])(implicit tx: G ~> F) extends AccountService[F] {

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

    val result: F[Option[(AccountRow, FollowCounts)]] = tx(accountDatabase.accountWithFollows(toRowId(id)))

    result.flatMap {
      case Some((acc, fs)) =>
        val moved: F[Option[Account]] = acc.movedToAccount match {
          case None            => F.pure(None)
          case Some(movedToId) => findAccount(toEntityId(movedToId))
        }

        moved.map { m =>
          Some(
            Account(
              EntityId[Account](acc.id.id),
              acc.username, acct(acc),
              acc.displayName,
              acc.locked,
              acc.createdAt,
              fs.followers,
              fs.following,
              0,
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
