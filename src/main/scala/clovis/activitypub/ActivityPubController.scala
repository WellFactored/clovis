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

package clovis.activitypub

import cats.Monad
import cats.implicits._
import io.circe.generic.auto._
import io.circe.refined._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait ActivityPubController[F[_]] {
  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]]
}

class ActivityPubControllerImpl[F[_]: Monad](service: ActivityPubService[F])
    extends ActivityPubController[F]
    with Http4sDsl[F]
    with CirceEntityDecoder {

  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]] = service.lookupPerson(name).flatMap {
    case Some(p) => ActorObject.of(p, hostDetails).fold(InternalServerError(_), a => Ok(a.asJson))
    case None    => NotFound()
  }
}
