package clovis.security
import cats.effect.IO
import org.scalatest.{FreeSpecLike, Matchers}

class RSAKeyPairTest extends FreeSpecLike with Matchers {

  "round trip" - {
    "when we generate a keypair" - {
      val create: IO[RSAKeyPairGenerator[IO]] = RSAKeyPairGenerator.create[IO]
      val kp:     RSAKeyPair                  = create.flatMap(_.generate).unsafeRunSync()

      "and convert the keys to strings" - {
        val pubString  = kp.encodedPublicKey
        val privString = kp.encodedPrivateKey

        "then a new key pair created from the strings" - {
          val kp2 = RSAKeyPair.of(privString, pubString)

          "should be identical to the original key pair" in {
            kp2 shouldBe kp
          }
        }
      }
    }
  }
}
