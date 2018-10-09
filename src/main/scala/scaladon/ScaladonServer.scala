package scaladon

import cats.effect._
import cats.implicits._
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeBuilder
import scaladon.database.DoobieAccountDB
import scaladon.services.{AccountService, AccountSvcImpl}

object ScaladonServer extends IOApp {
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:scaladon", // connect URL (driver-specific)
    "scaladon", // user
    "" // password
  )

  implicit val tx: ConnectionIO ~> IO = new ~>[ConnectionIO, IO] {
    override def apply[A](fa: ConnectionIO[A]): IO[A] =
      fa.transact(xa)
  }

  val accountDB     : DoobieAccountDB    = new DoobieAccountDB
  val accountService: AccountService[IO] = new AccountSvcImpl[IO, ConnectionIO](accountDB)

  def run(args: List[String]): IO[ExitCode] =
    ScaladonStream.stream[IO](accountService).compile.drain.as(ExitCode.Success)
}

object ScaladonStream {

  def scaladonRoutes[F[_] : Effect](accountService: AccountService[F]): HttpRoutes[F] = new ScaladonRoutes[F](accountService).routes

  def stream[F[_] : ConcurrentEffect](accountService: AccountService[F]): fs2.Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(scaladonRoutes(accountService), "/")
      .serve
}
