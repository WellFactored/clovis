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
import org.http4s.headers.{Accept, MediaRangeAndQValue, `Content-Type`}
import org.http4s.scalaxml.xmlEncoder
import org.http4s.util.CaseInsensitiveString

/**
  * Implement endpoints for the ".well-known" path (see https://tools.ietf.org/html/rfc5785)
  */
class WellKnownRoutes[F[_]: Sync](wellknownService: WellKnownService[F]) extends Http4sDsl[F] with MountableRoutes[F] with CirceEntityDecoder {

  object Resource extends QueryParamDecoderMatcher[String]("resource")

  private val applicationXrd  = new MediaType("application", "xrd+xml")
  private val xrdUTF8         = `Content-Type`(applicationXrd, Charset.`UTF-8`)
  private val applicationJson = new MediaType("application", "json")
  private val acceptHeader    = CaseInsensitiveString("Accept")

  case class Error(message: String)

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
          req.headers.get(acceptHeader).map(h => Accept.parse(h.value)) match {
            case None                => Ok(hm.toXML).map(_.withContentType(xrdUTF8))
            case Some(Left(failure)) => BadRequest(failure.asJson)
            case Some(Right(accept)) =>
              // See if any of the media types in the Accept header match what we will accept and find the
              // one with the highest qValue
              findAcceptableMediaTypes(accept, List(applicationJson, applicationXrd)).sortBy(_.qValue).lastOption match {
                case Some(mt) if mt.mediaRange.satisfiedBy(applicationXrd)  => Ok(hm.toXML).map(_.withContentType(xrdUTF8))
                case Some(mt) if mt.mediaRange.satisfiedBy(applicationJson) => Ok(hm.asJson.dropNulls)
                case _                                                      => NotAcceptable(Error(s"Do not recognize 'Accept' header of '${accept.toString}'").asJson)
              }
          }
        }

      // https://tools.ietf.org/html/rfc6415 allows for calling the host-meta.json instead of using an Accept header
      case GET -> Root / "host-meta.json" =>
        wellknownService.hostMeta.flatMap(hm => Ok(hm.asJson.dropNulls))
    }

  /**
    * Given an Accept header, which may contain a list of multiple media types, find those types that
    * satisfy our list of acceptable media types.
    */
  private def findAcceptableMediaTypes(accept: Accept, acceptableMediaTypes: List[MediaType]): List[MediaRangeAndQValue] =
    accept.values.filter(a => acceptableMediaTypes.exists(_.satisfies(a.mediaRange)))
}
