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

package clovis

import java.net.URI

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json, KeyEncoder}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import clovis.wellknown.WellKnownService

class WellKnownRoutes[F[_] : Sync](wellknownService: WellKnownService[F]) extends Http4sDsl[F] with HttpService[F] {
  object Resource extends QueryParamDecoderMatcher[String]("resource")

  implicit val uriEncoder   : Encoder[URI]    = Encoder.instance(uri => Json.fromString(uri.toString))
  implicit val uriKeyEncoder: KeyEncoder[URI] = KeyEncoder.instance(_.toString)

  private def dropNullValues(json: Json): Json =
    json
      .mapObject {
        _.filter { case (_, v) => !v.isNull }
      }
      .mapObject {
        _.mapValues {
          case o if o.isObject => dropNullValues(o)
          case a if a.isArray  => a.mapArray(_.map(dropNullValues))
          case v               => v
        }
      }

  override val mountPoint: String = "/.well-known"

  override val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "webfinger" :? Resource(resource) =>
        wellknownService.webfinger(resource).flatMap {
          case None      => NotFound()
          case Some(wfr) => Ok(dropNullValues(wfr.asJson))
        }
    }

}
