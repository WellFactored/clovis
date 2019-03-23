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
