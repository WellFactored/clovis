package clovis.activitypub
import cats.effect.Sync
import clovis.MountableRoutes
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class ActivityPubRoutes[F[_]: Sync](controller: ActivityPubController[F]) extends Http4sDsl[F] with MountableRoutes[F] {
  override def mountPoint: String = "/"

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "person" / name =>
        controller.getPerson(name)
    }
}
