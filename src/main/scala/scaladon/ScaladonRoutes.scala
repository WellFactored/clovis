package scaladon

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl._

class ScaladonRoutes[F[_] : Sync] extends Http4sDsl[F] {
  val routes: HttpRoutes[F] =
    new AccountsRoutes[F].routes
}


