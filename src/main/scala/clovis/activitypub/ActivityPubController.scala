package clovis.activitypub
import java.net.URL

import cats.effect.Sync
import cats.implicits._
import clovis.activitypub.models.PersonActor
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait ActivityPubController[F[_]] {
  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]]
}

case class ActorObject(
  `@context`:        String,
  `type`:            String,
  id:                URL,
  inbox:             URL,
  outbox:            URL,
  name:              String,
  preferredUsername: String
)

class ActivityPubControllerImpl[F[_]: Sync](service: ActivityPubService[F])
    extends ActivityPubController[F]
    with Http4sDsl[F]
    with CirceEntityDecoder {

  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]] = service.lookupPerson(name).flatMap {
    case Some(p) => Ok(actorObjectOf(p, hostDetails).asJson)
    case None    => NotFound()
  }

  implicit val urlEncoder: Encoder[URL] =
    (a: URL) => Json.fromString(a.toString)

  private def actorObjectOf(person: PersonActor, hostDetails: HostDetails): ActorObject = {
    val protocol: String = if (hostDetails.isSecure) "https" else "http"
    val urlBase:  String = s"$protocol://${hostDetails.host}"
    val idUrl = s"$urlBase/person/${person.name}"

    ActorObject(
      """["https://www.w3.org/ns/activitystreams", {"@language": "en"}]""",
      "Person",
      new URL(idUrl),
      new URL(s"$idUrl/inbox"),
      new URL(s"$idUrl/outbox"),
      person.name,
      person.name
    )
  }

}
