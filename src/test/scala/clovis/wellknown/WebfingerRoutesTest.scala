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

import java.net.URI

import cats.effect.IO
import cats.syntax.applicative._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.implicits._
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

class WebfingerRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount          = "known"
  val unknownAccount        = "unknown"
  val knownAcctURI          = new URI("/known/uri")
  private val webfingerPath = "/webfinger"

  type F[A] = IO[A]

  private val service = new WellKnownService[F] {
    override def webfinger(acct: String): F[Option[Webfinger]] =
      if (acct == knownAccount) Some(Webfinger(knownAcctURI, None, None, None)).pure[F]
      else None.pure[F]
    override def hostMeta: F[HostMeta] = HostMeta(Seq()).pure[F]
  }

  private val routes: HttpRoutes[F] = new WellKnownRoutes[F](service).routes

  "calling webfinger" - {
    "on a known account" - {
      val request:  Request[F]  = Request[F](Method.GET, buildUri(Some(knownAccount)))
      val response: Response[F] = routeRequest(request)

      "should respond with an OK" in {
        response.status.code shouldBe 200
      }

      "and have the correct data in the body" in {
        val result: Webfinger = response.as[Webfinger].unsafeRunSync()
        result.subject shouldBe knownAcctURI
      }
    }

    "on an unknown account should respond with 404" in {
      val request = Request[F](Method.GET, buildUri(Some(unknownAccount)))
      routeRequest(request).status.code shouldBe 404
    }

    "with no account value should respond with 404" in {
      val request = Request[F](Method.GET, buildUri(None))
      routes.orNotFound(request).unsafeRunSync().status.code shouldBe 404
    }

    "with no account parameter should respond with 404" in {
      val request = Request[F](Method.GET, Uri(path = webfingerPath))
      routes.orNotFound(request).unsafeRunSync().status.code shouldBe 404
    }
  }

  private def buildUri(account: Option[String]): Uri =
    Uri(path = webfingerPath, query = Query("resource" -> account))

  private def routeRequest(request: Request[F]): Response[F] =
    routes(request).value.unsafeRunSync() match {
      case None           => fail(s"No route was found to handle ${request.uri.toString()}")
      case Some(response) => response
    }
}
