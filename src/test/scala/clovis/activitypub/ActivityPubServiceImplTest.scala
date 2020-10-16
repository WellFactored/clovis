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
import cats.arrow.FunctionK
import cats.{Id, ~>}
import clovis.activitypub.models.PersonActor
import clovis.database.UserDatabase
import clovis.database.rows.{RowId, UserRow}
import clovis.security.{RSAKeyPair, RSAKeyPairGenerator}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

class ActivityPubServiceImplTest extends AnyFreeSpecLike with Matchers with OptionValues {
  private implicit val idK: Id ~> Id   = FunctionK.id[Id]
  val keyPair:              RSAKeyPair = RSAKeyPairGenerator.unsafeGenerateRSAKeyPair
  private val testUser = UserRow("username", keyPair.publicKey, keyPair.privateKey, RowId(-1))

  val userDatabase: UserDatabase[Id] =
    (name: String) => if (name == testUser.username) Some(testUser) else None

  val service = new ActivityPubServiceImpl[Id, Id](userDatabase)

  "lookupPerson" - {
    "should find an existing User and transform them to a PersonActor with the right name" in {
      service.lookupPerson(testUser.username) shouldBe Some(PersonActor(testUser.username))
    }

    "should return None if the user is not found" in {
      service.lookupPerson("unknown user name") shouldBe None
    }
  }
}
