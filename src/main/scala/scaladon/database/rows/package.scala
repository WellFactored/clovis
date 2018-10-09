package scaladon.database

import java.net.URL
import java.time.ZonedDateTime

import enumeratum.EnumEntry.CapitalWords
import enumeratum._

package object rows {
  type AccountId = RowId[AccountRow]

  sealed case class ActorType(override val entryName:String) extends EnumEntry
  object ActorType extends Enum[ActorType] {
    //noinspection TypeAnnotation
    override def values = findValues

    object Person extends ActorType("Person")
    object Service extends ActorType("Service")
  }

  case class AccountRow(
    username: String,
    domain: Option[String],
    displayName: String,
    locked: Boolean,
    createdAt: ZonedDateTime,
    followersCount: Int,
    followingCount: Int,
    statusesCount: Int,
    note: String,
    url: URL,
    avatar: URL,
    avatarStatic: URL,
    header: URL,
    headerStatic: URL,
    movedToAccount: Option[AccountId],
    actorType: ActorType,
    id: AccountId
  )

}
