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

import cats.effect.IO
import cats.implicits._
import clovis.activitypub.models.PersonActor
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s._
import org.http4s.circe._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

class GetPersonRoutesTest extends AnyFreeSpecLike with Matchers with CirceEntityDecoder with OptionValues {

  type F[A] = IO[A]

  val testUsername = "username"
  val testPersonActor: PersonActor = PersonActor(testUsername)

  val dummyService: ActivityPubService[F] =
    (username: String) => if (username == testUsername) Option(testPersonActor).pure[F] else Option.empty[PersonActor].pure[F]

  def controller(response: Option[PersonActor]): ActivityPubController[F] = new ActivityPubControllerImpl[F](dummyService)

  val routes: HttpRoutes[F] = new ActivityPubRoutes[F](controller(None), "local.domain").routes

  "GET /person should" - {
    "return a 404 Not Found if the name does not match a known user" in {
      val request:  Request[F]  = Request[F](Method.GET, buildUri("unknown"))
      val response: Response[F] = routeRequest(request)
      response.status shouldBe Status.NotFound
    }

    "if the name does match" - {
      val request: Request[F] = Request[F](Method.GET, buildUri(testUsername))
      val response = routeRequest(request)

      "it should return a 200 Ok" in { response.status shouldBe Status.Ok }
      val responseBody = response.as[ActorObject].attempt.unsafeRunSync()
      "and the response should contain a valid ActorObject" in { responseBody shouldBe a[Right[_, ActorObject]] }
      "and the ActorObject should" - {
        val actorObject = responseBody.toOption.value
        "have a type of Person" in { actorObject.`type`                                             shouldBe ActorType.Person }
        "and its preferredUsername set to the supplied username" in { actorObject.preferredUsername shouldBe testUsername }
      }
    }
  }

  private def buildUri(name: String): Uri =
    Uri(path = s"/person/$name")

  private def routeRequest(request: Request[F]): Response[F] =
    routes(request).value.unsafeRunSync() match {
      case None           => fail(s"No route was found to handle ${request.uri.toString()}")
      case Some(response) => response
    }

}
