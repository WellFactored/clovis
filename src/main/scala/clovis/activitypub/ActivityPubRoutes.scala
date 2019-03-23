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
import cats.effect.Sync
import clovis.MountableRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Request}

case class HostDetails(host: String, isSecure: Boolean)

object HostDetails {
  def from[F[_]](r: Request[F], localDomain: String): HostDetails = {
    val isSecure = r.isSecure.getOrElse(true) || r.headers.get(CaseInsensitiveString("X-Forwarded-Proto")).map(_.value.toLowerCase).contains("https")
    HostDetails(r.headers.get(CaseInsensitiveString("host")).map(_.value).getOrElse(localDomain), isSecure)
  }
}

class ActivityPubRoutes[F[_]: Sync](controller: ActivityPubController[F], localDomain: String) extends Http4sDsl[F] with MountableRoutes[F] {
  override def mountPoint: String = "/"

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case r @ GET -> Root / "person" / name =>
        controller.getPerson(name, HostDetails.from(r, localDomain))
    }
}
