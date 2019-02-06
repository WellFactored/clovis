package clovis.database.rows

case class UserRow(
  username:   String,
  publicKey:  String,
  privateKey: String,
  id:         UserId
)
