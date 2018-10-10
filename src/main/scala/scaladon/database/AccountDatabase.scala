package scaladon.database

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import scaladon.database.rows.{AccountId, AccountRow}

case class FollowCounts(followers: Int, following: Int)

trait AccountDatabase[F[_]] {
  def accountById(id: AccountId): F[Option[AccountRow]]
  def follows(id: AccountId): F[FollowCounts]
  def accountWithFollows(id: AccountId): F[Option[(AccountRow, FollowCounts)]]
}

class DoobieAccountDB extends AccountDatabase[ConnectionIO] with MetaHelpers {
  override def accountById(id: AccountId): ConnectionIO[Option[AccountRow]] = {
    sql"""select username, domain, display_name, locked, created_at, note, url, avatar, avatar_static, header, header_static, moved_to_account_id, actor_type, id from account where id = $id"""
      .query[AccountRow]
      .option
  }

  override def follows(id: AccountId): ConnectionIO[FollowCounts] = {
    sql"select  count(distinct fr.follower_id), count(distinct fd.followed_id) from follow fr, follow fd where fd.follower_id = $id and fr.followed_id = $id"
      .query[FollowCounts]
      .unique
  }

  def accountWithFollows(id: AccountId): ConnectionIO[Option[(AccountRow, FollowCounts)]] = {
    for {
      acc <- accountById(id)
      f <- follows(id)
    } yield (acc, f)
  }.map {
    case (Some(a), f) => Some((a, f))
    case (None, _)    => None
  }
}