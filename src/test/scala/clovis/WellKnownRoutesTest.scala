package clovis

import java.net.URI

import cats.effect._
import clovis.wellknown.{WebfingerResult, WellKnownService}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

class WellKnownRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount   = "known"
  val unknownAccount = "unknown"
  val knownAcctURI   = new URI("/known/uri")

  private val service = new WellKnownService[IO] {
    override def webfinger(acct: String): IO[Option[WebfingerResult]] = {
      if (acct == knownAccount) IO.pure(Some(WebfingerResult(knownAcctURI, None, None, None)))
      else IO.pure(None)
    }
  }

  private val routes = new WellKnownRoutes[IO](service).routes.orNotFound

  "calling webfinger" - {
    "on a known account should return an OK response with the correct data in the body" in {
      val request = Request[IO](Method.GET, Uri(path = "/webfinger", query = Query("resource" -> Some(knownAccount))))

      val response: Response[IO] = routeRequest(request)
      response.status.code shouldBe 200

      val result: WebfingerResult = response.as[WebfingerResult].unsafeRunSync()
      result.subject shouldBe knownAcctURI
    }

    "on an unknown account should respond with 404" in {
      val request = Request[IO](Method.GET, Uri(path = "/webfinger", query = Query("resource" -> Some(unknownAccount))))
      routeRequest(request).status.code shouldBe 404
    }
  }

  private def routeRequest(request: Request[IO]): Response[IO] =
    routes(request).unsafeRunSync()
}
