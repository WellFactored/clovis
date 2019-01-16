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

package clovis

import java.net.URL

import cats.effect.Sync
import cats.implicits._
import clovis.entities.{Account, EntityId}
import clovis.services.AccountService
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.util.Try

class AccountsRoutes[F[_]: Sync](accountService: AccountService[F]) extends Http4sDsl[F] with MountableService[F] {

  object MaxId extends OptionalQueryParamDecoderMatcher[Long]("max_id")
  object SinceId extends OptionalQueryParamDecoderMatcher[Long]("since_id")
  object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OnlyMedia extends OptionalQueryParamDecoderMatcher[Boolean]("only_media")
  object Pinned extends OptionalQueryParamDecoderMatcher[Boolean]("pinned")
  object ExcludeReplies extends OptionalQueryParamDecoderMatcher[Boolean]("exclude_replies")
  object AccountIds extends OptionalMultiQueryParamDecoderMatcher[String]("id")

  implicit val urlEncoder: Encoder[URL] = Encoder.instance(url => Json.fromString(url.toString))
  implicit def entityIDEncoder[T]: Encoder[EntityId[T]] =
    Encoder.instance(id => Json.fromLong(id.id))

  class EntityIdVar[T] {
    def unapply(s: String): Option[EntityId[T]] =
      if (!s.trim.isEmpty)
        Try(EntityId[T](s.toLong)).toOption
      else
        None
  }

  object AccountIdVar extends EntityIdVar[Account]

  override val mountPoint: String = "/api/v1/accounts"

  override val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / AccountIdVar(id) =>
        accountService.findAccount(id).flatMap {
          case None          => NotFound()
          case Some(account) => Ok(account.asJson.dropNulls)
        }

      case GET -> Root / "verify_credentials" =>
        NotImplemented()

      case q @ GET -> Root / "relationships" :? AccountIds(ids) =>
        ids.map(_ => NotImplemented()).getOrElse(BadRequest("missing mandatory 'id' parameter(s)"))

      case GET -> Root / "search" =>
        NotImplemented()

      case PATCH -> Root / "update_credentials" =>
        NotImplemented()

      case GET ->
            Root / LongVar(id) / "followers"
            :? MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case GET ->
            Root / LongVar(id) / "following"
            :? MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case GET -> Root / LongVar(id) / "statuses"
            :? OnlyMedia(onlyMedia) +& Pinned(pinned) +& ExcludeReplies(excludeReplies) +& MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "follow" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "unfollow" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "block" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "unblock" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "mute" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "unmute" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "pin" =>
        NotImplemented()

      case POST -> Root / LongVar(id) / "unpin" =>
        NotImplemented()

    }
}
