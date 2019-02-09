package clovis.security
import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

import clovis.security.RSAKeyPair.rsaKeyFactory

object RSAKeys {
  val base64Encoder: Base64.Encoder = Base64.getEncoder
  val base64Decoder: Base64.Decoder = Base64.getDecoder

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

case class RSAKeyPair(privateKey: RSAPrivateKey, publicKey: RSAPublicKey) {
  import RSAKeyPair.base64Encoder

  val encodedPrivateKey: String = base64Encoder.encodeToString(privateKey.getEncoded)
  val encodedPublicKey:  String = base64Encoder.encodeToString(publicKey.getEncoded)

}

object RSAKeyPair {
  import RSAKeys._
  val base64Encoder: Base64.Encoder = Base64.getEncoder
  val base64Decoder: Base64.Decoder = Base64.getDecoder

  val rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

  def of(encodedPrivateKey: String, encodedPublicKey: String): RSAKeyPair =
    new RSAKeyPair(decodePrivateKey(encodedPrivateKey), decodePublicKey(encodedPublicKey))
}
