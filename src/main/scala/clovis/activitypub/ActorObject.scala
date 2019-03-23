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
import cats.implicits._
import clovis.activitypub.models.PersonActor
import enumeratum.{CirceEnum, Enum, EnumEntry}
import eu.timepit.refined._
import eu.timepit.refined.string.Url

sealed abstract class ActorType extends EnumEntry
object ActorType extends Enum[ActorType] with CirceEnum[ActorType] {
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
