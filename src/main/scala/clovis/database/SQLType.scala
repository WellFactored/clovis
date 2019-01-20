package clovis.database
import scala.util.matching.Regex

case class SQLType(typ: String, nullable: Boolean, unique: Boolean, pk: Boolean) {
  def ddl: String = {
    val n = if (nullable) "" else " NOT NULL"
    val u = if (!pk && unique) " UNIQUE" else ""
    val p = if (pk) " PRIMARY KEY" else ""

    s"$typ$n$u$p".trim
  }
}

object SQLType {
  def apply(typ: String): SQLType = SQLType(typ, nullable = false, unique = false, pk = false)

  val OptionOf: Regex = "Option\\[(.+)\\]".r
  val RowIdOf:  Regex = "RowId\\[(.+)\\]".r

  def sqlTypeFor(typ: String): SQLType =
    typ match {
      case "String"        => SQLType("VARCHAR")
      case "Boolean"       => SQLType("BOOLEAN")
      case "URL"           => SQLType("VARCHAR")
      case "ZonedDateTime" => SQLType("TIMESTAMP(6) WITH TIME ZONE")
      case "ActorType"     => SQLType("VARCHAR")
      case OptionOf(t)     => sqlTypeFor(t).copy(nullable = true)
      case RowIdOf(t)      => SQLType("BIGINT")
      case _               => SQLType(typ)
    }
}


case class ColumnDecl(name: String, typ: SQLType) {
  def colDDL: String =
    s"$name ${typ.ddl}"
}
