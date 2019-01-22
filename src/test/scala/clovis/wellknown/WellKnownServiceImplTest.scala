package clovis.wellknown
import java.net.URL
import java.time.ZonedDateTime

import cats.arrow.FunctionK
import cats.{Id, ~>}
import clovis.database.rows.{AccountId, AccountRow, ActorType, RowId}
import clovis.database.{AccountDatabase, FollowCounts}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class WellKnownServiceImplTest extends WordSpecLike with Matchers with OptionValues {
  implicit val idK: Id ~> Id = FunctionK.id[Id]

  private val localDomain = "test.domain"

  val service = new WellKnownServiceImpl[Id, Id](localDomain, List(localDomain, "another.domain"), fakeAccountDatabase)

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

  private lazy val fakeAccountDatabase: AccountDatabase[Id] = new AccountDatabase[Id] {

    val accounts: List[AccountRow] = List(
      dummyAccount("test1", "test user 1")
    )

    override def accountById(id:        AccountId): Id[Option[AccountRow]]                      = ???
    override def accountByName(name:    String):    Id[Option[AccountRow]]                      = accounts.find(_.username == name)
    override def accountWithFollows(id: AccountId): Id[Option[(AccountRow, FollowCounts, Int)]] = ???
  }

  private val dummyURL = new URL(s"http://$localDomain")
  def dummyAccount(username: String, displayName: String): AccountRow =
    AccountRow(
      username,
      None,
      displayName,
      locked = false,
      ZonedDateTime.now,
      "",
      dummyURL,
      dummyURL,
      dummyURL,
      dummyURL,
      dummyURL,
      None,
      ActorType.Person,
      RowId(0))

}
