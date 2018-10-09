package scaladon

import cats.effect.Sync
import doobie.util.transactor.Transactor
import org.http4s._
import org.http4s.dsl._
import scaladon.services.AccountService

class ScaladonRoutes[F[_] : Sync](accountService: AccountService[F]) extends Http4sDsl[F] {
  val routes: HttpRoutes[F] =
    new AccountsRoutes[F](accountService).routes
}


