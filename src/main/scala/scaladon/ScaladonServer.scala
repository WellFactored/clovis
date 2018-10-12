package scaladon

import cats.effect._
import cats.implicits._
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scaladon.database.DoobieAccountDB
import scaladon.services.{AccountService, AccountSvcImpl}
import scaladon.wellknown.{WellKnownService, WellKnownSvcImpl}

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

  val localDomain = "scala.haus"

  val accountDB       : DoobieAccountDB      = new DoobieAccountDB
  val accountService  : AccountService[IO]   = new AccountSvcImpl[IO, ConnectionIO](accountDB)
  val webfingerService: WellKnownService[IO] = new WellKnownSvcImpl[IO, ConnectionIO](localDomain, List(localDomain), accountDB)

  def run(args: List[String]): IO[ExitCode] =
    ScaladonStream.stream[IO](accountService, webfingerService).compile.drain.as(ExitCode.Success)
}

object ScaladonStream {
  def stream[F[_] : ConcurrentEffect](accountService: AccountService[F], webfingerService: WellKnownService[F]): fs2.Stream[F, ExitCode] = {
    val services: Seq[HttpService[F]] = List(
      new AccountsRoutes[F](accountService),
      new WellKnownRoutes[F](webfingerService)
    )

    val router = Router(services.map(s => s.mountPoint -> s.routes): _*).orNotFound

    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
