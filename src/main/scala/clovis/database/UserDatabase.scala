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
import clovis.database.rows.UserRow
import clovis.security.RSAKeyMetaHelpers
import com.wellfactored.propertyinfo.{PropertyInfo, PropertyInfoGen}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.fragment.Fragment
import shapeless.LabelledGeneric

trait UserDatabase[F[_]] {
  def byName(name: String): F[Option[UserRow]]
}

object DoobieUserDB extends PropertyInfoGen {
  def decamelise(s: String): String = s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase

  implicit val lg: LabelledGeneric[UserRow] = LabelledGeneric[UserRow]
  val pi:          PropertyInfo[UserRow]    = implicitly[PropertyInfo[UserRow]]

  val fieldNames: String = pi.namesAndTypes.map(_.name).map(decamelise).mkString(", ")

  val allColumns: Fragment = Fragment.const0(fieldNames)
}

class DoobieUserDB extends UserDatabase[ConnectionIO] with MetaHelpers with RSAKeyMetaHelpers {
  private val selectUser =
    fr"""select""" ++ DoobieUserDB.allColumns ++ fr""" from "user""""

  override def byName(name: String): ConnectionIO[Option[UserRow]] =
    (selectUser ++ fr"""where username = $name""")
      .query[UserRow]
      .option
}
