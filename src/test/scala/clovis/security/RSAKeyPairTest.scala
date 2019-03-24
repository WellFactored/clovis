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

package clovis.security
import cats.effect.IO
import org.scalatest.{FreeSpecLike, Matchers}

class RSAKeyPairTest extends FreeSpecLike with Matchers {

  "round trip" - {
    "when we generate a keypair" - {
      val create: IO[RSAKeyPairGenerator[IO]] = RSAKeyPairGenerator.create[IO]
      val kp:     RSAKeyPair                  = create.flatMap(_.generate).unsafeRunSync()

      "and convert the keys to strings" - {
        val pubString  = kp.encodedPublicKey
        val privString = kp.encodedPrivateKey

        "then a new key pair created from the strings" - {
          val kp2 = RSAKeyPair.of(privString, pubString)

          "should be identical to the original key pair" in {
            kp2 shouldBe kp
          }
        }
      }
    }
  }
}
