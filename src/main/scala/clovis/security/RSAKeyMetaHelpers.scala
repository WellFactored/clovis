package clovis.security
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}

import doobie.util.Meta

trait RSAKeyMetaHelpers {
  import RSAKeys._

  implicit val rsaPublicKeyMeta: Meta[RSAPublicKey] =
    Meta[String].imap(decodePublicKey)(encodePublicKey)

  implicit val rsaPrivateKeyMeta: Meta[RSAPrivateKey] =
    Meta[String].imap(decodePrivateKey)(encodePrivateKey)
}
