package clovis.activitypub
import cats.effect.Sync
import cats.implicits._
import clovis.activitypub.models.PersonActor
import io.circe.Json
import io.circe.Json.obj
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait ActivityPubController[F[_]] {
  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]]
}

class ActivityPubControllerImpl[F[_]: Sync](service: ActivityPubService[F]) extends ActivityPubController[F] with Http4sDsl[F] {

  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]] = service.lookupPerson(name).flatMap {
    case Some(p) => Ok(actorObjectOf(p, hostDetails))
    case None    => NotFound()
  }

  private def actorObjectOf(person: PersonActor, hostDetails: HostDetails): Json = {
    val protocol: String = if (hostDetails.isSecure) "https" else "http"
    val urlBase:  String = s"$protocol//${hostDetails.host}"
    val idUrl = s"$urlBase/person/${person.name}"
    obj(
      Seq(
        "@context"          -> """["https://www.w3.org/ns/activitystreams", {"@language": "ja"}]""",
        "type"              -> "Person",
        "id"                -> idUrl,
        "inbox"             -> s"$idUrl/inbox",
        "outbox"            -> s"$idUrl/outbox",
        "name"              -> person.name,
        "preferredUsername" -> person.name
      ).map { case (k, v) => k -> Json.fromString(v) }: _*)
  }

}
