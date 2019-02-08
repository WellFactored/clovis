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

import java.net.URL
import java.sql.Timestamp
import java.time.{Instant, ZoneId, ZonedDateTime}

import clovis.database.rows.RowId
import doobie.util.Meta

/**
  * Define some `Meta` instances for common mappings from SQL types to Scala types.
  */
trait MetaHelpers {
  implicit def idMeta[T]: Meta[RowId[T]] =
    Meta[Long].imap(RowId[T])(_.id)

  implicit val urlMeta: Meta[URL] =
    Meta[String].imap(s => new URL(s))(_.toString)

  implicit val zonedDateTimeMeta: Meta[ZonedDateTime] =
    Meta[Timestamp].imap(ts => ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime), ZoneId.systemDefault))(
      zdt => new Timestamp(Instant.from(zdt).toEpochMilli)
    )
}
