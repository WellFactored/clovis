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
import clovis.activitypub.models.PersonActor
import org.scalatest.{EitherValues, FreeSpecLike, Matchers}

class ActorObjectSpec extends FreeSpecLike with Matchers with EitherValues {

  "ActorObject.of" - {
    "valid person and host details" - {
      val person             = PersonActor("name")
      val hostDetails        = HostDetails("localhost", isSecure = false)
      val actorObjectOrError = ActorObject.of(person, hostDetails)

      "should successfully build an ActorObject" in { actorObjectOrError shouldBe a[Right[_, ActorObject]] }
      val actorObject = actorObjectOrError.right.value
      // At the moment these are the only two properties we need to check.
      "with type of Person" in { actorObject.`type`                                                shouldBe ActorType.Person }
      "and a preferredUsername taken from PersonActor.username" in { actorObject.preferredUsername shouldBe person.username }
    }
  }
}
