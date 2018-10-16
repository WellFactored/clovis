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
