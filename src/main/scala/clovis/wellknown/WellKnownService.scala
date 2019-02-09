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
import clovis.database.UserDatabase
import clovis.database.rows.UserRow
import clovis.security.RSAKeys

trait WellKnownService[F[_]] {
  def webfinger(acct: String): F[Option[Webfinger]]
  def hostMeta: F[HostMeta]
}

class WellKnownServiceImpl[F[_]: Monad, G[_]](
  localDomain:      String,
  alternateDomains: List[String],
  userDatabase:     UserDatabase[G]
)(
  implicit tx: G ~> F
) extends WellKnownService[F] {
  private val F = Applicative[F]

  private val protocol = if (localDomain == "localhost") "http" else "https"

  override def hostMeta: F[HostMeta] = {
    val link = Link("lrdd", Some("application/xrd+xml"), None, None, None, Some(s"$protocol://$localDomain/.well-known/webfinger?resource={uri}"))
    F.pure(HostMeta(Seq(link)))
  }

  override def webfinger(acct: String): F[Option[Webfinger]] = {
    val UserRegex = "acct:(.*)@(.*)".r

    val username: Option[String] = acct match {
      case UserRegex(u, domain) if alternateDomains.contains(domain) => Some(u)
      case _                                                         => None
    }

    username match {
      case None =>
        F.pure(None)

      case Some(u) =>
        tx(userDatabase.byName(u)).map(_.map(toWebfingerResult))
    }
  }

  private def toWebfingerResult(user: UserRow): Webfinger = {
    val links = List(
      Link("http://webfinger.net/rel/profile-page", Some("text/html"), Some(shortAccountURL(user)), None, None, None),
      Link("self", Some("application/activity+json"), Some(userURL(user, None)), None, None, None),
      Link("magic-public-key", None, Some(magicKey(user)), None, None, None)
    )
    Webfinger(new URI(s"acct:${user.username}@$localDomain"), None, Some(links), None)
  }

  private def magicKey(user: UserRow): URI =
    new URI(s"data:application/magic-public-key,${RSAKeys.magicKeyString(user.publicKey)}")

  private def shortAccountURL(user: UserRow): URI =
    new URI(s"$protocol://$localDomain/@${user.username}")

  private def userURL(user: UserRow, format: Option[String]): URI = {
    val base = s"$protocol://$localDomain/users/${user.username}"

    format match {
      case None      => new URI(base)
      case Some(fmt) => new URI(s"$base.$fmt")
    }
  }
}
