package clovis.activitypub
import cats.effect.Sync
import cats.implicits._
import clovis.activitypub.models.PersonActor
import eu.timepit.refined._
import eu.timepit.refined.string.Url
import io.circe.generic.auto._
import io.circe.refined._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait ActivityPubController[F[_]] {
  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]]
}

sealed abstract class ActorType extends enumeratum.EnumEntry
object ActorType extends enumeratum.Enum[ActorType] with enumeratum.CirceEnum[ActorType] {
  //noinspection TypeAnnotation
  override val values = findValues

  case object Person extends ActorType
}

case class ActorObject(
  `@context`:        String, // TODO: Give this some proper structure
  `type`:            ActorType,
  id:                UrlString,
  inbox:             UrlString,
  outbox:            UrlString,
  name:              String,
  preferredUsername: String
)

object ActorObject {
  def of(person: PersonActor, hostDetails: HostDetails): Either[String, ActorObject] = {
    // TODO: Improve url construction. In particular, values being embedded in the strings aren't
    // currently being url-encoded.
    val protocol: String = if (hostDetails.isSecure) "https" else "http"
    val urlBase:  String = s"$protocol://${hostDetails.host}"
    val idUrlString = s"$urlBase/person/${person.username}"

    (refineV[Url](idUrlString), refineV[Url](s"$idUrlString/inbox"), refineV[Url](s"$idUrlString/outbox")).tupled.map {
      case (idUrl, inboxUrl, outboxUrl) =>
        ActorObject(
          "https://www.w3.org/ns/activitystreams",
          ActorType.Person,
          idUrl,
          inboxUrl,
          outboxUrl,
          person.username,
          person.username
        )
    }
  }
}

class ActivityPubControllerImpl[F[_]: Sync](service: ActivityPubService[F])
    extends ActivityPubController[F]
    with Http4sDsl[F]
    with CirceEntityDecoder {

  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]] = service.lookupPerson(name).flatMap {
    case Some(p) => ActorObject.of(p, hostDetails).fold(InternalServerError(_), a => Ok(a.asJson))
    case None    => NotFound()
  }
}
