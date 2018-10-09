package scaladon.database

import java.net.URL
import java.sql.Timestamp
import java.time.{Instant, ZoneId, ZonedDateTime}

import doobie.util.Meta
import scaladon.database.rows.{ActorType, RowId}

/**
  * Define some `Meta` instances for common mappings from SQL types to Scala types.
  */
trait MetaHelpers {
  implicit def idMeta[T]: Meta[RowId[T]] =
    Meta[Long].imap(RowId[T])(_.id)

  implicit val urlMeta: Meta[URL] =
    Meta[String].imap(s => new URL(s))(_.toString)

  implicit val zonedDateTimeMeta: Meta[ZonedDateTime] =
    Meta[Timestamp].imap(
      ts => ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime), ZoneId.systemDefault))(
      zdt => new Timestamp(Instant.from(zdt).toEpochMilli)
    )

  implicit val actorTypeMeta: Meta[ActorType] =
    Meta[String].imap(ActorType.withName)(_.entryName)
}
