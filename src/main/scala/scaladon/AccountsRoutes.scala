package scaladon

import java.net.URL

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import scaladon.entities.{Account, EntityId}
import scaladon.services.AccountService

import scala.util.Try

class AccountsRoutes[F[_] : Sync](accountService: AccountService[F]) extends Http4sDsl[F] with HttpService[F] {


  object MaxId extends OptionalQueryParamDecoderMatcher[Long]("max_id")
  object SinceId extends OptionalQueryParamDecoderMatcher[Long]("since_id")
  object Limit extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OnlyMedia extends OptionalQueryParamDecoderMatcher[Boolean]("only_media")
  object Pinned extends OptionalQueryParamDecoderMatcher[Boolean]("pinned")
  object ExcludeReplies extends OptionalQueryParamDecoderMatcher[Boolean]("exclude_replies")
  object AccountIds extends OptionalMultiQueryParamDecoderMatcher[String]("id")

  implicit val URLEncoder: Encoder[URL] = Encoder.instance(url => Json.fromString(url.toString))
  implicit def entityIDEncoder[T]: Encoder[EntityId[T]] = Encoder.instance(id => Json.fromLong(id.id))

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
          case Some(account) => Ok(account.asJson)
        }

      case GET -> Root / "verify_credentials" =>
        NotImplemented()

      case q@GET -> Root / "relationships" :? AccountIds(ids) =>
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
