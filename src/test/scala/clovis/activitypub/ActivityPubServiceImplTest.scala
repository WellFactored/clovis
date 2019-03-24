package clovis.activitypub
import cats.arrow.FunctionK
import cats.{Id, ~>}
import clovis.activitypub.models.PersonActor
import clovis.database.UserDatabase
import clovis.database.rows.{RowId, UserRow}
import clovis.security.{RSAKeyPair, RSAKeyPairGenerator}
import org.scalatest.{FreeSpecLike, Matchers, OptionValues}

class ActivityPubServiceImplTest extends FreeSpecLike with Matchers with OptionValues {
  private implicit val idK: Id ~> Id   = FunctionK.id[Id]
  val keyPair:              RSAKeyPair = RSAKeyPairGenerator.unsafeGenerateRSAKeyPair
  private val testUser = UserRow("username", keyPair.publicKey, keyPair.privateKey, RowId(-1))

  val userDatabase: UserDatabase[Id] =
    (name: String) => if (name == testUser.username) Some(testUser) else None

  val service = new ActivityPubServiceImpl[Id, Id](userDatabase)

  "lookupPerson" - {
    "should find an existing User and transform them to a PersonActor with the right name" in {
      service.lookupPerson(testUser.username) shouldBe Some(PersonActor(testUser.username))
    }

    "should return None if the user is not found" in {
      service.lookupPerson("unknown user name") shouldBe None
    }
  }
}
