package scaladon

import java.net.URL

import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import scaladon.entities.{Account, EntityId}
import scaladon.services.AccountService
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.auto._


class AccountsRoutes[F[_] : Sync](accountService: AccountService[F]) extends Http4sDsl[F] {
  val apiRoot: Path = Root / "api" / "v1"

  object MaxId extends OptionalQueryParamDecoderMatcher[Long]("max_id")
  object SinceId extends OptionalQueryParamDecoderMatcher[Long]("since_id")
  object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OnlyMedia extends OptionalQueryParamDecoderMatcher[Boolean]("only_media")
  object Pinned extends OptionalQueryParamDecoderMatcher[Boolean]("pinned")
  object ExcludeReplies extends OptionalQueryParamDecoderMatcher[Boolean]("exclude_replies")
  object AccountIds extends OptionalMultiQueryParamDecoderMatcher[String]("id")

  implicit val URLEncoder:Encoder[URL] = Encoder.instance(url => Json.fromString(url.toString))

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "api" / "v1" / "accounts" / LongVar(id) =>
        accountService.findAccount(EntityId[Account](id)).flatMap {
          case None          => NotFound()
          case Some(account) => Ok(account.asJson)
        }

      case GET -> Root / "api" / "v1" / "accounts" / "verify_credentials" =>
        NotImplemented()

      case q@GET -> Root / "api" / "v1" / "accounts" / "relationships" :? AccountIds(ids) =>
        ids.map(_ => NotImplemented()).getOrElse(BadRequest("missing mandatory 'id' parameter(s)"))


      case GET -> Root / "api" / "v1" / "accounts" / "search" =>
        NotImplemented()

      case PATCH -> Root / "api" / "v1" / "accounts" / "update_credentials" =>
        NotImplemented()

      case GET ->
        Root / "api" / "v1" / "accounts" / LongVar(id) / "followers"
        :? MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case GET ->
        Root / "api" / "v1" / "accounts" / LongVar(id) / "following"
        :? MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case GET -> Root / "api" / "v1" / "accounts" / LongVar(id) / "statuses"
        :? OnlyMedia(onlyMedia) +& Pinned(pinned) +& ExcludeReplies(excludeReplies) +& MaxId(maxId) +& SinceId(sinceId) +& Limit(limt) =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "follow" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "unfollow" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "block" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "unblock" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "mute" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "unmute" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "pin" =>
        NotImplemented()

      case POST -> Root / "api" / "v1" / "accounts" / LongVar(id) / "unpin" =>
        NotImplemented()

    }
}
