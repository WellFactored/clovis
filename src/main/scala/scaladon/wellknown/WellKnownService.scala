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
      Link("http://webfinger.net/rel/profile-page", Some("text/html"), Some(shortAccountURL(account))),
      Link("http://schemas.google.com/g/2010#updates-from", Some("application/atom+xml"), Some(accountURL(account, Some("atom")))),
      Link("self", Some("application/activity+json"), Some(accountURL(account, None))),
    )

    WebfingerResult(new URI(s"acct:${account.username}@$localDomain"), None, Some(links), None)
  }
  private def shortAccountURL(account: AccountRow): URI =
    new URI(s"https://$localDomain/@${account.username}")

  private def accountURL(account: AccountRow, format: Option[String]): URI = {
    val base = s"https://$localDomain/users/${account.username}"

    format match {
      case None      => new URI(base)
      case Some(fmt) => new URI(s"$base.$fmt")
    }
  }

}
