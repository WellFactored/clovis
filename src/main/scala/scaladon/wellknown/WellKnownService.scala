package scaladon.wellknown

import java.net.URI

import cats.implicits._
import cats.{Applicative, Monad, ~>}
import scaladon.database.AccountDatabase
import scaladon.database.rows.AccountRow

trait WellKnownService[F[_]] {
  def webfinger(acct: String): F[Option[WebfingerResult]]
}

class WellKnownSvcImpl[F[_] : Monad, G[_]](
  localDomain: String,
  alternateDomains: List[String],
  accountDatabase: AccountDatabase[G]
)(
  implicit tx: G ~> F
)
  extends WellKnownService[F] {
  private val F = Applicative[F]

  override def webfinger(acct: String): F[Option[WebfingerResult]] = {
    val User = "acct:(.*)@(.*)".r

    val username: Option[String] = acct match {
      case User(u, domain) if alternateDomains.contains(domain) => Some(u)
      case _                                                    => None
    }

    username match {
      case None =>
        F.pure(None)

      case Some(u) =>
        tx(accountDatabase.accountByName(u)).map(_.map(toWebfingerResult))
    }
  }

  private def toWebfingerResult(account: AccountRow): WebfingerResult = {
    val links = List(
      Link("http://webfinger.net/rel/profile-page", Some("text/html"), Some(new URI(s"https://$localDomain/@${account.username}")))
    )

    WebfingerResult(new URI(s"acct:${account.username}@$localDomain"), None, Some(links), None)
  }
}
