package clovis.wiring
import cats.effect._
import cats.~>
import clovis.activitypub._
import clovis.database.DoobieUserDB
import clovis.wellknown.{WellKnownRoutes, WellKnownService, WellKnownServiceImpl}
import clovis.{Config, MountableRoutes, TransactionSupport}
import com.softwaremill.macwire._

class AppWiring[F[_]: Async: ContextShift](config: Config) extends TransactionSupport {
  protected val localDomain: String = config.localDomain
  protected val userDB = new DoobieUserDB

  implicit val txK: ConnectionType ~> F = tx[F](config.dbConfig)

  lazy val webfingerService:      WellKnownService[F]      = wire[WellKnownServiceImpl[F, ConnectionType]]
  lazy val activityPubService:    ActivityPubService[F]    = wire[ActivityPubServiceImpl[F, ConnectionType]]
  lazy val activityPubController: ActivityPubController[F] = wire[ActivityPubControllerImpl[F]]

  lazy val wellKnownRoutes:   WellKnownRoutes[F]   = wire[WellKnownRoutes[F]]
  lazy val activityPubRoutes: ActivityPubRoutes[F] = wire[ActivityPubRoutes[F]]

  lazy val routes: List[MountableRoutes[F]] = List(wellKnownRoutes, activityPubRoutes)
}
