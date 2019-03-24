/*
 * Copyright (C) 2018  Well-Factored Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
