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
import java.security.KeyPairGenerator
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}

import cats.effect.Sync

class RSAKeyPairGenerator[F[_]: Sync](kpg: KeyPairGenerator) {
  def generate: F[RSAKeyPair] = Sync[F].delay {
    RSAKeyPairGenerator.generateRSAKeyPair(kpg)
  }
}

object RSAKeyPairGenerator {
  def create[F[_]: Sync]: F[RSAKeyPairGenerator[F]] = Sync[F].delay {
    val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(2048)
    new RSAKeyPairGenerator(kpg)
  }

  /**
    * Generate a key pair without managing the side effects. Useful for testing.
    */
  def unsafeGenerateRSAKeyPair: RSAKeyPair =
    generateRSAKeyPair(KeyPairGenerator.getInstance("RSA"))

  private def generateRSAKeyPair(kpg: KeyPairGenerator): RSAKeyPair = {
    val kp = kpg.generateKeyPair()
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val rsaPublic: RSAPublicKey = kp.getPublic.asInstanceOf[RSAPublicKey]

    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val rsaPrivate: RSAPrivateKey = kp.getPrivate.asInstanceOf[RSAPrivateKey]

    new RSAKeyPair(rsaPrivate, rsaPublic)
  }
}
