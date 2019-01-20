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

package clovis.database

import cats.~>
import clovis.database.rows.{AccountId, AccountRow}
import com.wellfactored.propertyinfo._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.fragment.Fragment
import shapeless.LabelledGeneric

case class FollowCounts(followers: Int, following: Int)

trait AccountDatabase[F[_]] {
  def accountById(id:        AccountId): F[Option[AccountRow]]
  def accountByName(name:    String):    F[Option[AccountRow]]
  def accountWithFollows(id: AccountId): F[Option[(AccountRow, FollowCounts, Int)]]
}

object DoobieAccountDB extends PropertyInfoGen {
  def decamelise(s: String): String = s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase

  implicit val lg: LabelledGeneric[AccountRow] = LabelledGeneric[AccountRow]
  val pi:          PropertyInfo[AccountRow]    = implicitly[PropertyInfo[AccountRow]]

  val fieldNames: String = pi.namesAndTypes.map(_.name).map(decamelise).mkString(", ")

  val allColumns: Fragment = Fragment.const0(fieldNames)

  // Experimental attempt at generating DDL for creating table
  def ddl(): Unit =
    pi.namesAndTypes.map(t => ColumnDecl(decamelise(t.name), SQLType.sqlTypeFor(t.ty.describe))).foreach(d => println(d.colDDL))
}

class DoobieAccountDB extends AccountDatabase[ConnectionIO] with MetaHelpers {
  private val selectAccount =
    fr"""select""" ++ DoobieAccountDB.allColumns ++ fr""" from account"""

  override def accountById(id: AccountId): ConnectionIO[Option[AccountRow]] =
    (selectAccount ++ fr"""where id = $id""")
      .query[AccountRow]
      .option

  private def followCounts(id: AccountId): ConnectionIO[FollowCounts] =
    sql"select  count(distinct fr.follower_id), count(distinct fd.followed_id) from follow fr, follow fd where fd.follower_id = $id and fr.followed_id = $id"
      .query[FollowCounts]
      .unique

  private def statusCount(id: AccountId): ConnectionIO[Int] =
    sql"select count(id) from status where account_id = $id"
      .query[Int]
      .unique

  def accountWithFollows(id: AccountId): ConnectionIO[Option[(AccountRow, FollowCounts, Int)]] = {
    for {
      acc <- accountById(id)
      f   <- followCounts(id)
      s   <- statusCount(id)
    } yield (acc, f, s)
  }.map {
    case (Some(a), f, s) => Some((a, f, s))
    case (None, _, _)    => None
  }

  override def accountByName(name: String): ConnectionIO[Option[AccountRow]] =
    (selectAccount ++ fr"where username = $name and domain is null")
      .query[AccountRow]
      .option
}

class EmbeddedAccountDB[F[_]](doobie: DoobieAccountDB, tx: ConnectionIO ~> F) extends AccountDatabase[F] {
  override def accountById(id:        AccountId): F[Option[AccountRow]] = tx(doobie.accountById(id))
  override def accountByName(name:    String): F[Option[AccountRow]] = tx(doobie.accountByName(name))
  override def accountWithFollows(id: AccountId): F[Option[(AccountRow, FollowCounts, Int)]] =
    tx(doobie.accountWithFollows(id))
}
