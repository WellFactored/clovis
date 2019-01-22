package clovis
import cats.data.EitherT
import ciris._
import ciris.api.{Applicative, Sync}

case class DBConfig(url:       String, username: String, password: Secret[String])
case class Config(localDomain: String, port:     Int, dbConfig:    DBConfig)

class ConfigLoader[F[_]: Sync: Applicative] {
  private val dbURL      = envF[F, Option[String]]("JDBC_DATABASE_URL").mapValue(_.getOrElse("jdbc:postgresql:clovis"))
  private val dbUser     = envF[F, Option[String]]("JDBC_DATABASE_USERNAME").mapValue(_.getOrElse("clovis"))
  private val dbPassword = envF[F, Option[Secret[String]]]("JDBC_DATABASE_PASSWORD").mapValue(_.getOrElse(Secret("")))
  private val dbConfig   = loadConfig(dbURL, dbUser, dbPassword)(DBConfig.apply)

  private val localDomain = envF[F, Option[String]]("LOCAL_DOMAIN").mapValue(_.getOrElse("scala.haus"))

  private val httpPort = propF[F, Option[Int]]("http.port").mapValue(_.getOrElse(8080))

  val load: F[Either[ConfigErrors, Config]] = EitherT(loadConfig(localDomain, httpPort, dbConfig)(Config.apply).result).value
}
