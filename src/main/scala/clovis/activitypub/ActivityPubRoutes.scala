package clovis.activitypub
import cats.effect.Sync
import clovis.MountableRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Request}

case class HostDetails(host: String, isSecure: Boolean)
object HostDetails {
  def from[F[_]](r: Request[F], localDomain: String): HostDetails = {
    val isSecure = r.isSecure.getOrElse(true) || r.headers.get(CaseInsensitiveString("X-Forwarded-Proto")).map(_.value.toLowerCase).contains("https")
    HostDetails(r.headers.get(CaseInsensitiveString("host")).map(_.value).getOrElse(localDomain), isSecure)
  }
}

class ActivityPubRoutes[F[_]: Sync](controller: ActivityPubController[F], localDomain: String) extends Http4sDsl[F] with MountableRoutes[F] {
  override def mountPoint: String = "/"

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case r @ GET -> Root / "person" / name =>
        controller.getPerson(name, HostDetails.from(r, localDomain))
    }
}
