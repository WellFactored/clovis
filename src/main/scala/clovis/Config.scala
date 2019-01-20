package clovis
import cats.data.EitherT
import cats.effect.IO
import ciris.cats._
import ciris.{ConfigErrors, envF, loadConfig}

case class Config(localDomain: String, dbURL: String, dbUser: String, dbPassword: String)

object Config {

  private val localDomain = envF[IO, Option[String]]("LOCAL_DOMAIN").mapValue(_.getOrElse("scala.haus"))
  private val dbURL       = envF[IO, Option[String]]("JDBC_DATABASE_URL").mapValue(_.getOrElse("jdbc:postgresql:clovis"))
  private val dbUser      = envF[IO, Option[String]]("JDBC_DATABASE_USERNAME").mapValue(_.getOrElse("clovis"))
  private val dbPassword  = envF[IO, Option[String]]("JDBC_DATABASE_PASSWORD").mapValue(_.getOrElse(""))

  val load: IO[Either[ConfigErrors, Config]] = EitherT(loadConfig(localDomain, dbURL, dbUser, dbPassword)(Config.apply).result).value

}
