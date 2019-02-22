package clovis.activitypub
import clovis.activitypub.models.PersonActor
import org.scalatest.{EitherValues, FreeSpecLike, Matchers}

class ActorObjectSpec extends FreeSpecLike with Matchers with EitherValues {

  "ActorObject.of" - {
    "valid person and host details" - {
      val person             = PersonActor("name")
      val hostDetails        = HostDetails("localhost", isSecure = false)
      val actorObjectOrError = ActorObject.of(person, hostDetails)

      "should successfully build an ActorObject" in { actorObjectOrError shouldBe a[Right[_, ActorObject]] }
      val actorObject = actorObjectOrError.right.value
      // At the moment these are the only two properties we need to check.
      "with type of Person" in { actorObject.`type`                                                shouldBe ActorType.Person }
      "and a preferredUsername taken from PersonActor.username" in { actorObject.preferredUsername shouldBe person.username }
    }
  }
}
