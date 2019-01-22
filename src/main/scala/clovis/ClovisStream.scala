package clovis
import cats.effect.{ConcurrentEffect, ExitCode}
import clovis.services.AccountService
import clovis.wellknown.{WellKnownRoutes, WellKnownService}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.util.Try

object ClovisStream {
  def stream[F[_]: ConcurrentEffect](accountService: AccountService[F], webfingerService: WellKnownService[F]): fs2.Stream[F, ExitCode] = {
    val services: Seq[MountableService[F]] = List(
      new AccountsRoutes[F](accountService),
      new WellKnownRoutes[F](webfingerService)
    )

    val router = Router(services.map(s => s.mountPoint -> s.routes): _*).orNotFound

    val port =
      Option(System.getProperty("http.port"))
        .flatMap(s => Try(s.toInt).toOption)
        .getOrElse(8080)

    BlazeServerBuilder[F]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
