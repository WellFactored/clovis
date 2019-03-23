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

package clovis.wiring
import cats.effect._
import cats.~>
import clovis.activitypub._
import clovis.database.DoobieUserDB
import clovis.wellknown.{WellKnownRoutes, WellKnownService, WellKnownServiceImpl}
import clovis.{Config, MountableRoutes, TransactionSupport}
import com.softwaremill.macwire._

class AppWiring[F[_]: Async: ContextShift](config: Config) extends TransactionSupport {
  protected val localDomain: String = config.localDomain
  protected val userDB = new DoobieUserDB

  implicit val txK: ConnectionType ~> F = tx[F](config.dbConfig)

  lazy val webfingerService:      WellKnownService[F]      = wire[WellKnownServiceImpl[F, ConnectionType]]
  lazy val activityPubService:    ActivityPubService[F]    = wire[ActivityPubServiceImpl[F, ConnectionType]]
  lazy val activityPubController: ActivityPubController[F] = wire[ActivityPubControllerImpl[F]]

  lazy val wellKnownRoutes:   WellKnownRoutes[F]   = wire[WellKnownRoutes[F]]
  lazy val activityPubRoutes: ActivityPubRoutes[F] = wire[ActivityPubRoutes[F]]

  lazy val routes: List[MountableRoutes[F]] = List(wellKnownRoutes, activityPubRoutes)
}
