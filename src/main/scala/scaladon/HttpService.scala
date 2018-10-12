/*
 * Copyright (C) 2018  com.wellfactored
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

package scaladon

import org.http4s.HttpRoutes

/**
  * `HttpService` combines a set of routes with a mount point. These form the parameters to the
  * `BlazeBuilder.mountService`
  */
trait HttpService[F[_]] {
  def routes: HttpRoutes[F]
  def mountPoint: String
}
