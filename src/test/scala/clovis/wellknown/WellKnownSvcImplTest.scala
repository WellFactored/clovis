package clovis.wellknown
import java.net.URL
import java.time.ZonedDateTime

import cats.arrow.FunctionK
import cats.{Id, ~>}
import clovis.database.rows.{AccountId, AccountRow, ActorType, RowId}
import clovis.database.{AccountDatabase, FollowCounts}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class WellKnownSvcImplTest extends WordSpecLike with Matchers with OptionValues {
  implicit val idK: Id ~> Id = FunctionK.id[Id]

  val service = new WellKnownSvcImpl[Id, Id]("test.domain", List("test.domain"), fakeAccountDatabase)

  "webfinger" should {
    "return a result for a known user" in {
      service.webfinger("acct:test1@test.domain") shouldBe a[Some[_]]
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

  private val dummyURL = new URL("http://test.domain")
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
