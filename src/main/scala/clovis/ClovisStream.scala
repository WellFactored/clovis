package clovis
import cats.arrow.FunctionK
import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

class ClovisStream[F[_]: ConcurrentEffect: Timer](startupPort: Port, routes: List[MountableRoutes[F]]) {
  def start: fs2.Stream[F, ExitCode] = {
    val router = Logger(logHeaders = true, logBody = false, FunctionK.id[F])(Router(routes.map(s => s.mountPoint -> s.routes): _*).orNotFound)

    BlazeServerBuilder[F]
      .bindHttp(startupPort.value, "0.0.0.0")
      .withHttpApp(router)
      .serve
  }
}
