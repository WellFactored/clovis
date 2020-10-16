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
import cats.effect.{Async, ContextShift}
import cats.implicits._
import ciris._

case class Port(value: Int)

case class DBConfig(url: String, username: String, password: Secret[String])

case class Config(localDomain: String, startupPort: Port, dbConfig: DBConfig)

class ConfigLoader[F[_]: ContextShift: Async] {
  implicit def portDecoder: ConfigDecoder[String, Port] = ConfigDecoder[String, Int].map(Port)

  private val dbURL      = env("JDBC_DATABASE_URL").as[String].default("jdbc:postgresql:clovis")
  private val dbUser     = env("JDBC_DATABASE_USERNAME").as[String].default("clovis")
  private val dbPassword = env("JDBC_DATABASE_PASSWORD").as[String].secret.default(Secret(""))
  private val dbConfig   = (dbURL, dbUser, dbPassword).parMapN(DBConfig)

  private val localDomain = env("LOCAL_DOMAIN").as[String].default("localhost")

  private val httpPort = prop("http.port").as[Port].default(Port(8080))

  val load: F[Either[ConfigError, Config]] = (localDomain, httpPort, dbConfig).parMapN(Config).attempt[F]
}
