package clovis
package wellknown

import java.io.StringReader

import cats.arrow.FunctionK
import cats.effect.IO
import clovis.database.rows.{AccountId, AccountRow}
import clovis.database.{AccountDatabase, FollowCounts}
import org.http4s.{HttpRoutes, Method, Request, Response, _}
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

import scala.xml.XML

class HostMetaRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount         = "known"
  val unknownAccount       = "unknown"
  private val hostMetaPath = "/host-meta"

  private val dummyAccountDB = new AccountDatabase[IO] {
    override def accountById(id:        AccountId): IO[Option[AccountRow]]                      = ???
    override def accountByName(name:    String):    IO[Option[AccountRow]]                      = ???
    override def accountWithFollows(id: AccountId): IO[Option[(AccountRow, FollowCounts, Int)]] = ???
  }

  implicit val idK: FunctionK[IO, IO] = FunctionK.id[IO]

  private val service = new WellKnownServiceImpl[IO, IO]("local.domain", List("local.domain"), dummyAccountDB)
  private val routes: HttpRoutes[IO] = new WellKnownRoutes[IO](service).routes

  "calling host-meta" - {
    val request:  Request[IO]  = Request[IO](Method.GET, Uri(path = hostMetaPath))
    val response: Response[IO] = routeRequest(request)

    "should respond with an OK" in {
      response.status.code shouldBe 200
    }

    "and have the correct data in the body" in {

      val body: String = response.bodyAsText.compile.lastOrError.unsafeRunSync()
      val xml  = XML.load(new StringReader(body))
      val lrdd = (xml \ "Link").find(_.attribute("rel").map(_.text).contains("lrdd"))

      (lrdd.value \@ "template") should include("https://local.domain")
    }
  }

  private def routeRequest(request: Request[IO]): Response[IO] =
    routes(request).value.unsafeRunSync() match {
      case None           => fail(s"No route was found to handle ${request.uri.toString()}")
      case Some(response) => response
    }
}
