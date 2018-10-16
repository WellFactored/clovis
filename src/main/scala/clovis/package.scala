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

import java.net.URI

import cats.syntax.either._
import io.circe._

import scala.util.Try

/**
  * Just a bunch of useful stuff
  */
package object clovis {
  implicit val uriEncoder: Encoder[URI] = Encoder.instance(uri => Json.fromString(uri.toString))


  implicit val uriDecoder: Decoder[URI] = Decoder.instance { uri =>
    uri.value.asString match {
      case Some(s) => Try(new URI(s)).toEither.leftMap(t => DecodingFailure(t.getMessage, List()))
      case None    => DecodingFailure(s"${uri.value} is not a string", List()).asLeft
    }
  }

  implicit val uriKeyEncoder: KeyEncoder[URI] = KeyEncoder.instance(_.toString)

  implicit val uriKeyDecoder: KeyDecoder[URI] = KeyDecoder.instance(s => Try(new URI(s)).toOption)

  def dropNullValues(json: Json): Json =
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

  implicit class JsonSyntax(json: Json) {
    def dropNulls: Json = dropNullValues(json)
  }
}
