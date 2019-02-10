package clovis.wellknown
import cats.arrow.FunctionK
import cats.effect.IO
import cats.{Id, ~>}
import clovis.database.UserDatabase
import clovis.database.rows._
import clovis.security.{RSAKeyPair, RSAKeyPairGenerator}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class WellKnownServiceImplTest extends WordSpecLike with Matchers with OptionValues {
  implicit val idK: Id ~> Id = FunctionK.id[Id]

  private val localDomain = "test.domain"

  private val dummyKeyPair: RSAKeyPair =
    RSAKeyPairGenerator.create[IO].flatMap(_.generate).unsafeRunSync()

  val service = new WellKnownServiceImpl[Id, Id](localDomain, fakeAccountDatabase)

  "hostMeta" should {
    "return a link that contains the local domain" in {
      val lrdd     = service.hostMeta.links.find(_.rel.contains("lrdd"))
      val template = lrdd.value.template
      template.value should include(localDomain)
    }
  }

  "webfinger" should {
    "return a webfinger result for a known user" in {
      service.webfinger(s"acct:test1@$localDomain") shouldBe a[Some[_]]
    }

    "not return a result when the user is unknown" in {
      service.webfinger(s"acct:unknown@$localDomain") shouldBe None
    }
    "not return a result when the domain is unknown" in {
      service.webfinger("acct:test1@unknown.domain") shouldBe None
    }
  }

  private lazy val fakeAccountDatabase: UserDatabase[Id] = new UserDatabase[Id] {

    val users: List[UserRow] = List(dummyUser("test1"))

    override def byName(name: String): Id[Option[UserRow]] = users.find(_.username == name)
  }

  def dummyUser(username: String): UserRow =
    UserRow(
      username,
      dummyKeyPair.publicKey,
      dummyKeyPair.privateKey,
      RowId(0)
    )

}
