package clovis.security
import java.security.KeyPairGenerator
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}

import cats.effect.Sync

class RSAKeyPairGenerator[F[_]: Sync](kpg: KeyPairGenerator) {
  def generate: F[RSAKeyPair] = Sync[F].delay {
    val kp = kpg.generateKeyPair()
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val rsaPublic: RSAPublicKey = kp.getPublic.asInstanceOf[RSAPublicKey]

    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    val rsaPrivate: RSAPrivateKey = kp.getPrivate.asInstanceOf[RSAPrivateKey]

    new RSAKeyPair(rsaPrivate, rsaPublic)
  }
}

object RSAKeyPairGenerator {
  def create[F[_]: Sync]: F[RSAKeyPairGenerator[F]] = Sync[F].delay {
    val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(2048)
    new RSAKeyPairGenerator(kpg)
  }
}
