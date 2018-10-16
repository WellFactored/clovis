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

import cats.effect._
import cats.implicits._
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import clovis.database.DoobieAccountDB
import clovis.services.{AccountService, AccountSvcImpl}
import clovis.wellknown.{WellKnownService, WellKnownSvcImpl}

object ClovisServer extends IOApp {
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:clovis", // connect URL (driver-specific)
    "clovis", // user
    "" // password
  )

  implicit val tx: ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    override def apply[A](fa: ConnectionIO[A]): IO[A] =
      fa.transact(xa)
  }

  val localDomain = "scala.haus"

  val accountDB       : DoobieAccountDB      = new DoobieAccountDB
  val accountService  : AccountService[IO]   = new AccountSvcImpl[IO, ConnectionIO](accountDB)
  val webfingerService: WellKnownService[IO] = new WellKnownSvcImpl[IO, ConnectionIO](localDomain, List(localDomain), accountDB)

  def run(args: List[String]): IO[ExitCode] =
    ClovisStream.stream[IO](accountService, webfingerService).compile.drain.as(ExitCode.Success)
}

object ClovisStream {
  def stream[F[_] : ConcurrentEffect](accountService: AccountService[F], webfingerService: WellKnownService[F]): fs2.Stream[F, ExitCode] = {
    val services: Seq[MountableService[F]] = List(
      new AccountsRoutes[F](accountService),
      new WellKnownRoutes[F](webfingerService)
    )

    val router = Router(services.map(s => s.mountPoint -> s.routes): _*).orNotFound

    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
