package clovis
import java.net.URI

import io.circe.Json
import org.scalatest._

class packageTest extends FreeSpecLike with Matchers with OptionValues with EitherValues {
  "uriDecoder" - {
    "should give an error" - {
      "when uri is None" in {
        uriDecoder.decodeJson(Json.Null).left.value.message shouldBe "null is not a string"
      }

      "when uri is not valid" in {
        uriDecoder.decodeJson(Json.fromString("not a valid uri")).left.value.message shouldBe "Illegal character in path at index 3: not a valid uri"
      }
    }

    "should give a valid URI" in {
      val validUri = "https://local.domain"
      uriDecoder.decodeJson(Json.fromString(validUri)).right.value shouldBe new URI(validUri)
    }
  }
}
