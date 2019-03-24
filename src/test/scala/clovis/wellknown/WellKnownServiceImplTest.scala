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

package clovis.wellknown
import cats.arrow.FunctionK
import cats.effect.IO
import cats.{Id, ~>}
import clovis.database.UserDatabase
import clovis.database.rows._
import clovis.security.{RSAKeyPair, RSAKeyPairGenerator}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class WellKnownServiceImplTest extends WordSpecLike with Matchers with OptionValues {
  implicit val idK: Id ~> Id = FunctionK.id[Id]

  private val localDomain = "test.domain"

  private val dummyKeyPair: RSAKeyPair =
    RSAKeyPairGenerator.create[IO].flatMap(_.generate).unsafeRunSync()

  val service = new WellKnownServiceImpl[Id, Id](localDomain, fakeAccountDatabase)

  "hostMeta" should {
    "return a link that contains the local domain" in {
      val lrdd     = service.hostMeta.links.find(_.rel.contains("lrdd"))
      val template = lrdd.value.template
      template.value should include(localDomain)
    }
  }

  "webfinger" should {
    "return a webfinger result for a known user" in {
      service.webfinger(s"acct:test1@$localDomain") shouldBe a[Some[_]]
    }

    "not return a result when the user is unknown" in {
      service.webfinger(s"acct:unknown@$localDomain") shouldBe None
    }
    "not return a result when the domain is unknown" in {
      service.webfinger("acct:test1@unknown.domain") shouldBe None
    }
  }

  private lazy val fakeAccountDatabase: UserDatabase[Id] = new UserDatabase[Id] {

    val users: List[UserRow] = List(dummyUser("test1"))

    override def byName(name: String): Id[Option[UserRow]] = users.find(_.username == name)
  }

  def dummyUser(username: String): UserRow =
    UserRow(
      username,
      dummyKeyPair.publicKey,
      dummyKeyPair.privateKey,
      RowId(0)
    )

}
