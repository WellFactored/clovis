/*
 * Copyright (C) 2018  Well-Factored Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package clovis.wellknown

import java.net.URI

import cats.implicits._
import cats.{Applicative, Monad, ~>}
import clovis.database.AccountDatabase
import clovis.database.rows.AccountRow

trait WellKnownService[F[_]] {
  def webfinger(acct: String): F[Option[WebfingerResult]]
  def hostMeta: F[HostMeta]
}

class WellKnownServiceImpl[F[_]: Monad, G[_]](
  localDomain:      String,
  alternateDomains: List[String],
  accountDatabase:  AccountDatabase[G]
)(
  implicit tx: G ~> F
) extends WellKnownService[F] {
  private val F = Applicative[F]

  override def hostMeta: F[HostMeta] =
    F.pure(
      HostMeta(
        Seq(
          Link("lrdd", Some("application/xrd+xml"), None, None, None, Some(s"https://$localDomain/.well-known/webfinger?resource={uri}"))
        )
      )
    )

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
      Link("http://webfinger.net/rel/profile-page", Some("text/html"), Some(shortAccountURL(account)), None, None, None),
      Link("self", Some("application/activity+json"), Some(accountURL(account, None)), None, None, None),
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
