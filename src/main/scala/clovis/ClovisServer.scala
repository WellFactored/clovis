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
import cats.effect._
import cats.implicits._
import cats.~>
import ciris.cats._
import ciris.{envF, _}
import clovis.database.DoobieAccountDB
import clovis.services.{AccountService, AccountSvcImpl}
import clovis.wellknown.{WellKnownRoutes, WellKnownService, WellKnownSvcImpl}
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.util.Try

object ClovisServer extends IOApp {
  def xa(url: String, user: String, password: String): Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    url,
    user,
    password
  )

  def tx(url: String, user: String, password: String): ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    override def apply[A](fa: ConnectionIO[A]): IO[A] =
      fa.transact(xa(url, user, password))
  }

  case class Config(localDomain: String, dbURL: String, dbUser: String, dbPassword: String)

  private val localDomain = envF[IO, Option[String]]("LOCAL_DOMAIN").mapValue(_.getOrElse("scala.haus"))
  private val dbURL       = envF[IO, Option[String]]("JDBC_DATABASE_URL").mapValue(_.getOrElse("jdbc:postgresql:clovis"))
  private val dbUser      = envF[IO, Option[String]]("JDBC_DATABASE_USERNAME").mapValue(_.getOrElse("clovis"))
  private val dbPassword  = envF[IO, Option[String]]("JDBC_DATABASE_PASSWORD").mapValue(_.getOrElse(""))

  val config: EitherT[IO, ConfigErrors, Config] = EitherT(loadConfig(localDomain, dbURL, dbUser, dbPassword)(Config).result)

  val accountDB: DoobieAccountDB = new DoobieAccountDB

  val stream: IO[ExitCode] = config.value.flatMap {
    case Left(errs) =>
      errs.messages.foreach(System.err.println)
      IO.pure(ExitCode.Error)

    case Right(c) =>
      implicit val txx:     ConnectionIO ~> IO   = tx(c.dbURL, c.dbUser, c.dbPassword)
      val accountService:   AccountService[IO]   = new AccountSvcImpl[IO, ConnectionIO](accountDB)
      val webfingerService: WellKnownService[IO] = new WellKnownSvcImpl[IO, ConnectionIO](c.localDomain, List(c.localDomain), accountDB)
      ClovisStream
        .stream[IO](accountService, webfingerService)
        .compile[IO, IO, ExitCode]
        .drain
        .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    stream
}

object ClovisStream {
  def stream[F[_]: ConcurrentEffect](accountService: AccountService[F], webfingerService: WellKnownService[F]): fs2.Stream[F, ExitCode] = {
    val services: Seq[MountableService[F]] = List(
      new AccountsRoutes[F](accountService),
      new WellKnownRoutes[F](webfingerService)
    )

    val router = Router(services.map(s => s.mountPoint -> s.routes): _*).orNotFound

    val port =
      Option(System.getProperty("http.port"))
        .flatMap(s => Try(s.toInt).toOption)
        .getOrElse(8080)

    BlazeServerBuilder[F]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
