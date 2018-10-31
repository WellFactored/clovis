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

package clovis.database.rows

import java.net.URL
import java.time.ZonedDateTime

case class AccountRow(
  username: String,
  domain: Option[String],
  displayName: String,
  locked: Boolean,
  createdAt: ZonedDateTime,
  note: String,
  url: URL,
  avatar: URL,
  avatarStatic: URL,
  header: URL,
  headerStatic: URL,
  movedToAccount: Option[AccountId],
  actorType: ActorType,
  id: AccountId
)
