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
import ciris.cats.effect._
import clovis.database.DoobieAccountDB
import clovis.services.{AccountService, AccountSvcImpl}
import clovis.wellknown.{WellKnownService, WellKnownServiceImpl}

object ClovisServer extends IOApp with TransactionSupport {
  private val configLoader = new ConfigLoader[IO]
  private val accountDB: DoobieAccountDB = new DoobieAccountDB

  def run(args: List[String]): IO[ExitCode] =
    configLoader.load.flatMap {
      case Left(errs) =>
        errs.messages.foreach(System.err.println)
        IO.pure(ExitCode.Error)

      case Right(c) =>
        implicit val txK:     ConnectionType ~> IO = tx(c.dbConfig)
        val accountService:   AccountService[IO]   = new AccountSvcImpl[IO, ConnectionType](accountDB)
        val webfingerService: WellKnownService[IO] = new WellKnownServiceImpl[IO, ConnectionType](c.localDomain, List(c.localDomain), accountDB)

        ClovisStream
          .stream[IO](accountService, webfingerService)
          .compile[IO, IO, ExitCode]
          .drain
          .as(ExitCode.Success)
    }
}


