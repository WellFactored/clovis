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

package clovis
import cats.data.EitherT
import ciris._
import ciris.api.{Applicative, Sync}

case class Port(value: Int)

case class DBConfig(url: String, username: String, password: Secret[String])

case class Config(localDomain: String, startupPort: Port, dbConfig: DBConfig)

class ConfigLoader[F[_]: Sync: Applicative] {
  implicit def portDecoder[A](
    implicit decoder: ConfigDecoder[A, Int]
  ): ConfigDecoder[A, Port] =
    decoder.mapOption("Port")(i => Some(Port(i)))

  private val dbURL      = envF[F, Option[String]]("JDBC_DATABASE_URL").mapValue(_.getOrElse("jdbc:postgresql:clovis"))
  private val dbUser     = envF[F, Option[String]]("JDBC_DATABASE_USERNAME").mapValue(_.getOrElse("clovis"))
  private val dbPassword = envF[F, Option[Secret[String]]]("JDBC_DATABASE_PASSWORD").mapValue(_.getOrElse(Secret("")))
  private val dbConfig   = loadConfig(dbURL, dbUser, dbPassword)(DBConfig.apply)

  private val localDomain = envF[F, Option[String]]("LOCAL_DOMAIN").mapValue(_.getOrElse("localhost"))

  private val httpPort = propF[F, Option[Port]]("http.port").mapValue(_.getOrElse(Port(8080)))

  val load: F[Either[ConfigErrors, Config]] = EitherT(loadConfig(localDomain, httpPort, dbConfig)(Config.apply).result).value
}
