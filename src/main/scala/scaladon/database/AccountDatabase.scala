package scaladon.database

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import scaladon.database.rows.{AccountId, AccountRow}

trait AccountDatabase[F[_]] {
  def accountById(id: AccountId): F[Option[AccountRow]]
}

class DoobieAccountDB extends AccountDatabase[ConnectionIO] with MetaHelpers {
  override def accountById(id: AccountId): ConnectionIO[Option[AccountRow]] = {
    sql"""select username, domain, display_name, locked, created_at, followers_count, following_count, statuses_count, note, url, avatar, avatar_static, header, header_static, moved_to_account_id, actor_type, id from account where id = $id"""
      .query[AccountRow]
      .to[List]
      .map(_.headOption)
  }
}