package clovis.database
import clovis.database.rows.UserRow
import com.wellfactored.propertyinfo.{PropertyInfo, PropertyInfoGen}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.fragment.Fragment
import com.wellfactored.propertyinfo._
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

class DoobieUserDB extends UserDatabase[ConnectionIO] with MetaHelpers {
  private val selectUser =
    fr"""select""" ++ DoobieUserDB.allColumns ++ fr""" from account"""

  override def byName(name: String): ConnectionIO[Option[UserRow]] =
    (selectUser ++ fr"""where username = $name""")
      .query[UserRow]
      .option
}
