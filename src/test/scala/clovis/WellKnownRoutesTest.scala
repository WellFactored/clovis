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

import java.net.URI

import cats.effect._
import clovis.wellknown.{WebfingerResult, WellKnownService}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

class WellKnownRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount   = "known"
  val unknownAccount = "unknown"
  val knownAcctURI   = new URI("/known/uri")

  private val service = new WellKnownService[IO] {
    override def webfinger(acct: String): IO[Option[WebfingerResult]] = {
      if (acct == knownAccount) IO.pure(Some(WebfingerResult(knownAcctURI, None, None, None)))
      else IO.pure(None)
    }
  }

  private val routes = new WellKnownRoutes[IO](service).routes.orNotFound

  "calling webfinger" - {
    "on a known account should return an OK response with the correct data in the body" in {
      val request = Request[IO](Method.GET, Uri(path = "/webfinger", query = Query("resource" -> Some(knownAccount))))

      val response: Response[IO] = routeRequest(request)
      response.status.code shouldBe 200

      val result: WebfingerResult = response.as[WebfingerResult].unsafeRunSync()
      result.subject shouldBe knownAcctURI
    }

    "on an unknown account should respond with 404" in {
      val request = Request[IO](Method.GET, Uri(path = "/webfinger", query = Query("resource" -> Some(unknownAccount))))
      routeRequest(request).status.code shouldBe 404
    }
  }

  private def routeRequest(request: Request[IO]): Response[IO] =
    routes(request).unsafeRunSync()
}
