package clovis.database.rows
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}

case class UserRow(
  username:   String,
  publicKey:  RSAPublicKey,
  privateKey: RSAPrivateKey,
  id:         UserId
)
