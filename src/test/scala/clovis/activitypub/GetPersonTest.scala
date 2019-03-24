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
import clovis.activitypub.models.PersonActor
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s.Status
import org.http4s.circe._
import org.scalatest.{EitherValues, FreeSpecLike, Matchers}

class GetPersonTest extends FreeSpecLike with Matchers with CirceEntityDecoder with EitherValues {
  val localhost = HostDetails("localhost", isSecure = false)
  def dummyService(response: Option[PersonActor]): ActivityPubService[IO] = new ActivityPubService[IO] {
    override def lookupPerson(username: String): IO[Option[models.PersonActor]] = IO.pure(response)
  }

  def controller(response: Option[PersonActor]): ActivityPubControllerImpl[IO] = new ActivityPubControllerImpl[IO](dummyService(response))

  "getPerson should" - {
    "return a 404 Not Found if the name does not match a known user" in {
      controller(None).getPerson("doesn't matter", localhost).unsafeRunSync().status shouldBe Status.NotFound
    }
    "if the name does match" - {
      val username = "username"
      val response = controller(Some(PersonActor(username))).getPerson(username, localhost).unsafeRunSync()
      "it should return a 200 Ok" in { response.status shouldBe Status.Ok }
      val responseBody = response.as[ActorObject].attempt.unsafeRunSync()
      "and the response should contain a valid ActorObject" in { responseBody shouldBe a[Right[_, ActorObject]] }
      "and the ActorObject should" - {
        val actorObject = responseBody.right.value
        "have a type of Person" in { actorObject.`type`                                             shouldBe ActorType.Person }
        "and its preferredUsername set to the supplied username" in { actorObject.preferredUsername shouldBe username }
      }
    }
  }
}
