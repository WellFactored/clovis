package clovis.activitypub

import cats.Monad
import cats.implicits._
import io.circe.generic.auto._
import io.circe.refined._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait ActivityPubController[F[_]] {
  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]]
}

class ActivityPubControllerImpl[F[_]: Monad](service: ActivityPubService[F])
    extends ActivityPubController[F]
    with Http4sDsl[F]
    with CirceEntityDecoder {

  def getPerson(name: String, hostDetails: HostDetails): F[Response[F]] = service.lookupPerson(name).flatMap {
    case Some(p) => ActorObject.of(p, hostDetails).fold(InternalServerError(_), a => Ok(a.asJson))
    case None    => NotFound()
  }
}
