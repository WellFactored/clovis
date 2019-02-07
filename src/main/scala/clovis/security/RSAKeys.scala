package clovis.security
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.{KeyPair, KeyPairGenerator}
import java.util.Base64

object RSAKeys {
  val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
  kpg.initialize(2048)
  val kp: KeyPair = kpg.generateKeyPair()

  val encoder: Base64.Encoder = Base64.getEncoder

  val pvFormat = kp.getPrivate.getFormat
  val puFormat = kp.getPublic.getAlgorithm

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  val rsaPublic:  RSAPublicKey  = kp.getPublic.asInstanceOf[RSAPublicKey]
  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  val rsaPrivate: RSAPrivateKey = kp.getPrivate.asInstanceOf[RSAPrivateKey]

  val s = Seq(
    "RSA",
    encoder.encodeToString(rsaPublic.getModulus.toByteArray),
    encoder.encodeToString(rsaPublic.getPublicExponent.toByteArray)
  ).mkString(".")
}
