package clovis.security
import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

case class RSAKeyPair(privateKey: RSAPrivateKey, publicKey: RSAPublicKey) {
  import RSAKeyPair.base64Encoder

  val encodedPrivateKey: String = base64Encoder.encodeToString(privateKey.getEncoded)
  val encodedPublicKey:  String = base64Encoder.encodeToString(publicKey.getEncoded)

  val magicKey: String = Seq(
    "RSA",
    base64Encoder.encodeToString(publicKey.getModulus.toByteArray),
    base64Encoder.encodeToString(publicKey.getPublicExponent.toByteArray)
  ).mkString(".")
}

object RSAKeyPair {
  val base64Encoder: Base64.Encoder = Base64.getEncoder
  val base64Decoder: Base64.Decoder = Base64.getDecoder

  val rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

  def of(encodedPrivateKey: String, encodedPublicKey: String): RSAKeyPair = {
    val spec = new X509EncodedKeySpec(base64Decoder.decode(encodedPublicKey))
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val pub = rsaKeyFactory.generatePublic(spec).asInstanceOf[RSAPublicKey]

    val privateSpec = new PKCS8EncodedKeySpec(base64Decoder.decode(encodedPrivateKey))
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val priv = rsaKeyFactory.generatePrivate(privateSpec).asInstanceOf[RSAPrivateKey]

    new RSAKeyPair(priv, pub)
  }
}
