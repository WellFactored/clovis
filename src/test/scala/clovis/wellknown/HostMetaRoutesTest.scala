package clovis
package wellknown

import cats.arrow.FunctionK
import cats.effect.IO
import clovis.database.rows.{AccountId, AccountRow}
import clovis.database.{AccountDatabase, FollowCounts}
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.scalaxml.xml
import org.http4s.{HttpRoutes, Method, Request, Response, _}
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

import scala.xml.Elem

class HostMetaRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount         = "known"
  val unknownAccount       = "unknown"
  private val hostMetaPath = "/host-meta"

  type F[A] = IO[A]

  private val dummyAccountDB = new AccountDatabase[F] {
    override def accountById(id:        AccountId): F[Option[AccountRow]]                      = ???
    override def accountByName(name:    String):    F[Option[AccountRow]]                      = ???
    override def accountWithFollows(id: AccountId): F[Option[(AccountRow, FollowCounts, Int)]] = ???
  }

  implicit val idK: FunctionK[F, F] = FunctionK.id[F]

  private val service = new WellKnownServiceImpl[F, F]("local.domain", List("local.domain"), dummyAccountDB)
  private val routes: HttpRoutes[F] = new WellKnownRoutes[F](service).routes

  "calling host-meta with no Accept header" - {
    val request:  Request[F]  = Request[F](Method.GET, Uri(path = hostMetaPath))
    val response: Response[F] = routeRequest(request)

    "should respond with an OK" in {
      response.status.code shouldBe 200
    }

    "and have the correct XML data in the body" in {
      val body = response.as[Elem].unsafeRunSync()
      val lrdd = (body \ "Link").find(_.attribute("rel").map(_.text).contains("lrdd"))

      (lrdd.value \@ "template") should include("https://local.domain")
    }
  }

  "calling host-meta with Accept=application/json" - {
    val request:  Request[F]  = Request[F](Method.GET, Uri(path = hostMetaPath), headers = Headers(Header("Accept", "application/json")))
    val response: Response[F] = routeRequest(request)

    "should respond with an OK" in {
      response.status.code shouldBe 200
    }

    "and have the correct data in the body" in {
      val body = response.as[HostMeta].unsafeRunSync()
      val lrdd = body.links.find(_.rel.contains("lrdd"))

      lrdd.value.template.value should include("https://local.domain")
    }
  }

  private def routeRequest(request: Request[F]): Response[F] =
    routes(request).value.unsafeRunSync() match {
      case None           => fail(s"No route was found to handle ${request.uri.toString()}")
      case Some(response) => response
    }
}
