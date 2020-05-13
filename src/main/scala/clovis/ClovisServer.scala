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
import ciris.cats.effect._
import clovis.wiring.AppWiring

object ClovisServer extends IOApp with TransactionSupport {
  private val configLoader = new ConfigLoader[IO]

  def run(args: List[String]): IO[ExitCode] =
    configLoader.load.flatMap {
      case Left(errs) =>
        errs.messages.foreach(System.err.println)
        IO.pure(ExitCode.Error)

      case Right(c) =>
        val wiring = new AppWiring[IO](c)

        new ClovisStream[IO](c.startupPort, wiring.routes).start
          .compile[IO, IO, ExitCode]
          .drain
          .as(ExitCode.Success)
    }
}
