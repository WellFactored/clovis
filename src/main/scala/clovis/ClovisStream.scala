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
import cats.arrow.FunctionK
import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext

class ClovisStream[F[_]: ConcurrentEffect: Timer](startupPort: Port, routes: List[MountableRoutes[F]])(implicit ec: ExecutionContext) {
  def start: fs2.Stream[F, ExitCode] = {
    val router = Logger(logHeaders = true, logBody = false, FunctionK.id[F])(Router(routes.map(s => s.mountPoint -> s.routes): _*).orNotFound)

    BlazeServerBuilder[F](ec)
      .bindHttp(startupPort.value, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
