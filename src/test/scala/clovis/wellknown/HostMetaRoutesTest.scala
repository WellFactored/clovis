package clovis
package wellknown

import cats.arrow.FunctionK
import cats.effect.IO
import clovis.database.UserDatabase
import clovis.database.rows.UserRow
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.scalaxml.xml
import org.http4s.util.CaseInsensitiveString
import org.http4s.{HttpRoutes, Method, Request, Response, _}
import org.scalatest.{EitherValues, FreeSpecLike, Matchers, OptionValues}

import scala.xml.Elem

class HostMetaRoutesTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  val knownAccount             = "known"
  val unknownAccount           = "unknown"
  private val hostMetaPath     = "/host-meta"
  private val hostMetaJsonPath = "/host-meta.json"

  val acceptHeadersThatShouldReturnXML = List(
    "application/xrd+xml",
    "application/xrd+xml; charset=utf8",
    "*/*",
    "application/*",
    "application/xrd+xml; q=1.0, application/json; q=0.5",
    "application/json; q=0.5, application/xrd+xml; q=1.0"
  )

  val acceptHeadersThatShouldReturnJson = List(
    "application/json",
    "application/xrd+xml; q=0.5, application/json; q=1.0",
    "application/json; q=1.0, application/xrd+xml; q=0.5"
  )

  "calling host-meta" - {
    "with no Accept header" - { shouldReturnXML(callWithoutAccept) }

    acceptHeadersThatShouldReturnXML.foreach { a =>
      s"with Accept=$a" - { shouldReturnXML(callWithAccept(a)) }
    }

    def shouldReturnXML(response: Response[F]): Unit = {
      "should respond with an OK" in { response.status.code shouldBe 200 }

      "and have a Content-Type of xrd+xml" in {
        response.headers.get(CaseInsensitiveString("Content-Type")).value.value shouldBe "application/xrd+xml; charset=UTF-8"
      }

      "and have the correct XML data in the body" in {
        val body = response.as[Elem].unsafeRunSync()
        val lrdd = (body \ "Link").find(_.attribute("rel").map(_.text).contains("lrdd"))

        (lrdd.value \@ "template") should include("https://local.domain")
      }
    }

    acceptHeadersThatShouldReturnJson.foreach { a =>
      s"with Accept=$a" - {
        val response: Response[F] = callWithAccept(a)

        "should respond with an OK" in {
          response.status.code shouldBe 200
        }

        "and have the correct data in the body" in {
          val body = response.as[HostMeta].unsafeRunSync()
          val lrdd = body.links.find(_.rel.contains("lrdd"))

          lrdd.value.template.value should include("https://local.domain")
        }

        "and have a Content-Type of application/json" in {
          response.headers.get(CaseInsensitiveString("Content-Type")).value.value shouldBe "application/json"
        }
      }
    }

    "with Accept=application/csv" - {
      val request:  Request[F]  = Request[F](Method.GET, Uri(path = hostMetaPath), headers = Headers(Header("Accept", "application/csv")))
      val response: Response[F] = routeRequest(request)

      "should respond with an Unacceptable" in {
        response.status.code shouldBe 406
      }
    }
  }

  "calling host-meta.json path" - {
    val response: Response[F] = routeRequest(Request[F](Method.GET, Uri(path = hostMetaJsonPath)))

    "should respond with an OK" in {
      response.status.code shouldBe 200
    }

    "and have the correct data in the body" in {
      val body = response.as[HostMeta].unsafeRunSync()
      val lrdd = body.links.find(_.rel.contains("lrdd"))

      lrdd.value.template.value should include("https://local.domain")
    }

    "and have a Content-Type of application/json" in {
      response.headers.get(CaseInsensitiveString("Content-Type")).value.value shouldBe "application/json"
    }
  }

  type F[A] = IO[A]
  implicit val idK: FunctionK[F, F] = FunctionK.id[F]

  private lazy val service = new WellKnownServiceImpl[F, F]("local.domain", List("local.domain"), stubUserDB)
  private lazy val routes: HttpRoutes[F] = new WellKnownRoutes[F](service).routes

  private def routeRequest(request: Request[F]): Response[F] =
    routes(request).value.unsafeRunSync() match {
      case None           => fail(s"No route was found to handle ${request.uri.toString()}")
      case Some(response) => response
    }

  private def callWithAccept(acceptString: String): Response[F] =
    routeRequest(Request[F](Method.GET, Uri(path = hostMetaPath), headers = Headers(Header("Accept", acceptString))))

  private def callWithoutAccept: Response[F] =
    routeRequest(Request[F](Method.GET, Uri(path = hostMetaPath)))

  private lazy val stubUserDB = new UserDatabase[F] {
    override def byName(name: String): F[Option[UserRow]] = IO.pure(None)
  }
}
