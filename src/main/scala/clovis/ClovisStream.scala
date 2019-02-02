package clovis
import cats.effect.{ConcurrentEffect, ExitCode}
import clovis.services.AccountService
import clovis.wellknown.{WellKnownRoutes, WellKnownService}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object ClovisStream {
  def stream[F[_]: ConcurrentEffect](
    httpPort:         Int,
    accountService:   AccountService[F],
    webfingerService: WellKnownService[F]): fs2.Stream[F, ExitCode] = {
    val services: Seq[MountableService[F]] = List(
      new AccountsRoutes[F](accountService),
      new WellKnownRoutes[F](webfingerService)
    )

    val router = Logger(logHeaders = true, logBody = false)(Router(services.map(s => s.mountPoint -> s.routes): _*).orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(httpPort, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
