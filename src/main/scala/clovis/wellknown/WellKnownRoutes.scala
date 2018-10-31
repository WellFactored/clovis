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
package wellknown

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.util.CaseInsensitiveString

import scala.xml.Elem

/**
  * Implement endpoints for the "well-known" path (see https://tools.ietf.org/html/rfc5785)
  */
class WellKnownRoutes[F[_]: Sync](wellknownService: WellKnownService[F])
    extends Http4sDsl[F]
    with MountableService[F]
    with CirceEntityDecoder {

  object Resource extends QueryParamDecoderMatcher[String]("resource")

  private val xrd: MediaType = new MediaType("application", "xrd+xml")
  //private val jrd    : MediaType      = new MediaType("application", "jrd+json")
  private val xrdUTF8: `Content-Type` = `Content-Type`(xrd, Charset.`UTF-8`)
  //private val jrdUTF8: `Content-Type` = `Content-Type`(jrd, Charset.`UTF-8`)
  private val acceptHeader = CaseInsensitiveString("Accept")

  override val mountPoint: String = "/.well-known"

  override val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      // https://tools.ietf.org/html/rfc7033
      case GET -> Root / "webfinger" :? Resource(resource) =>
        wellknownService.webfinger(resource).flatMap {
          case None      => NotFound()
          case Some(wfr) => Ok(wfr.asJson.dropNulls)
        }

      // https://tools.ietf.org/html/rfc6415
      case req @ GET -> Root / "host-meta" =>
        wellknownService.hostMeta.flatMap { hm =>
          req.headers.get(acceptHeader).map(_.value) match {
            case Some("application/json") => Ok(hm.asJson.dropNulls)
            case _                        => Ok(toXML(hm).toString).map(_.withContentType(xrdUTF8))
          }
        }
    }

  private def linksAsXML(links: Seq[Link]): Seq[Elem] =
    links.map(link =>
      <Link rel="lrdd" type="application/xrd+xml" template={link.template.getOrElse("")}/>)

  private def toXML(hostMeta: HostMeta): Elem =
    <XRD xmlns="http://docs.oasis-open.org/ns/xri/xrd-1.0">
      {linksAsXML(hostMeta.links)}
    </XRD>
}
