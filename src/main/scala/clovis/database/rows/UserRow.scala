package clovis.database.rows

case class UserRow(
  username:   String,
  domain:     Option[String],
  publicKey:  String,
  privateKey: String,
  id:         UserId
)
