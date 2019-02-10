package clovis.activitypub
import cats.data.Nested
import cats.implicits._
import cats.{Monad, ~>}
import clovis.activitypub.models.PersonActor
import clovis.database.UserDatabase

trait ActivityPubService[F[_]] {
  def lookupPerson(username: String): F[Option[PersonActor]]
}

class ActivityPubServiceImpl[F[_]: Monad, G[_]](userDatabase: UserDatabase[G])(implicit tx: G ~> F) extends ActivityPubService[F] {
  override def lookupPerson(username: String): F[Option[PersonActor]] =
    Nested(tx(userDatabase.byName(username)))
      .map(u => PersonActor(u.username))
      .value
}
