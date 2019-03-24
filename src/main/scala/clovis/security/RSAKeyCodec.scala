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
import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

object RSAKeyCodec {
  private val base64Encoder: Base64.Encoder = Base64.getEncoder
  private val base64Decoder: Base64.Decoder = Base64.getDecoder

  // TODO: Creating a KeyFactor is an effectful operation. I need to eliminate this
  //  line and use `RSAKeyPairGenerator` or similar instead
  private val rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def decodePublicKey(encodedPublicKey: String): RSAPublicKey = {
    val spec = new X509EncodedKeySpec(base64Decoder.decode(encodedPublicKey))
    rsaKeyFactory.generatePublic(spec).asInstanceOf[RSAPublicKey]
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def decodePrivateKey(encodedPrivateKey: String): RSAPrivateKey = {
    val privateSpec = new PKCS8EncodedKeySpec(base64Decoder.decode(encodedPrivateKey))

    rsaKeyFactory.generatePrivate(privateSpec).asInstanceOf[RSAPrivateKey]
  }

  def encodePrivateKey(privateKey: RSAPrivateKey): String = base64Encoder.encodeToString(privateKey.getEncoded)
  def encodePublicKey(publicKey:   RSAPublicKey):  String = base64Encoder.encodeToString(publicKey.getEncoded)

  def magicKeyString(publicKey: RSAPublicKey): String =
    Seq(
      "RSA",
      base64Encoder.encodeToString(publicKey.getModulus.toByteArray),
      base64Encoder.encodeToString(publicKey.getPublicExponent.toByteArray)
    ).mkString(".")
}
