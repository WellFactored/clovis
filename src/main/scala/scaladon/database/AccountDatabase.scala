package scaladon.database

import java.net.URL
import java.sql.Timestamp
import java.time.{Instant, ZoneId, ZonedDateTime}

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.Meta
import scaladon.database.rows.{AccountId, AccountRow, ActorType, RowId}

trait AccountDatabase[F[_]] {
  def accountById(id: AccountId): F[Option[AccountRow]]
}

class DoobieAccountDB extends AccountDatabase[ConnectionIO] {
  implicit val accountIdMeta: Meta[RowId[AccountRow]] =
    Meta[Long].imap(RowId[AccountRow])(_.id)

  implicit val urlMeta: Meta[URL] =
    Meta[String].imap(s => new URL(s))(_.toString)

  implicit val zonedDateTimeMeta: Meta[ZonedDateTime] =
    Meta[Timestamp].imap(
      ts => ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime), ZoneId.systemDefault))(
      zdt => new Timestamp(Instant.from(zdt).toEpochMilli)
    )

  implicit val actorTypeMeta: Meta[ActorType] =
    Meta[String].imap(ActorType.withName)(_.entryName)

  override def accountById(id: AccountId): ConnectionIO[Option[AccountRow]] = {
    sql"""select username, domain, display_name, locked, created_at, followers_count, following_count, statuses_count, note, url, avatar, avatar_static, header, header_static, moved_to_account_id, actor_type, id from account where id = $id"""
      .query[AccountRow]
      .to[List]
      .map(_.headOption)
  }
}