package scaladon

import java.net.{URI, URL}
import java.time.ZonedDateTime

import enumeratum.EnumEntry.Lowercase
import enumeratum._

package object models {

  case class Id[T](id: Long)

  type AccountId = Id[Account]

  case class Account(
    id: AccountId,
    username: String,
    acct: String,
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
    emojis: List[Emoji],
    moved: Option[String],
    fields: Option[Map[String, String]],
    bot: Option[Boolean]
  )

  case class Application(name: String, website: Option[URL])

  type AttachmentId = Id[Attachment]
  case class Attachment(
    id: AttachmentId,
    _type: String,
    url: URL,
    remoteURL: Option[URL],
    previewURL: URL,
    textURL: Option[URL],
    meta: Option[Map[String, Object]],
    description: Option[String]
  )

  type OEmbedData = String

  case class Card(
    url: URL,
    title: String,
    description: String,
    image: Option[URL],
    _type: String,
    authorName: Option[OEmbedData],
    authorURL: Option[OEmbedData],
    providerName: Option[OEmbedData],
    providerURL: Option[OEmbedData],
    html: Option[OEmbedData],
    width: Option[OEmbedData],
    height: Option[OEmbedData],
  )

  case class Context(ancestors: List[Status], descendents: List[Status])

  case class Emoji(shortcode: String, staticUrl: String, url: URL, visibleInPicker: String)

  case class Error(error: String)

  type FilterId = Id[Filter]
  case class Filter(id: FilterId, phrase: String, context: String, expiresAt: Option[ZonedDateTime], irreversible: Boolean, wholeWord: Boolean)

  case class Instance(
    uri: URI,
    title: String,
    description: String,
    email: String,
    version: String,
    //urls:streamingAPI,
    languages: List[String],
    contactAccount: Account
  )

  // Know as `List` in Mastodon - renamed here for obvious reasons
  type MembershipListId = Id[MembershipList]
  case class MembershipList(id: MembershipListId, title: String)

  case class Mention(URL: URL, username: String, acct: String, id: AccountId)

  type NotificationId = Id[Notification]
  case class Notification(id: NotificationId, _type: String, createdAt: ZonedDateTime, account: AccountId, status: Option[StatusId])

  type PushSubscriptionId = Id[PushSubscription]
  case class PushSubscription(id: PushSubscriptionId, endpoint: URL, serverKey: String, alerts: Map[String, Boolean])

  case class Relationship(
    id: AccountId,
    following: Boolean,
    followedBy: Boolean,
    blocking: Boolean,
    muting: Boolean,
    mutingNotifications: Boolean,
    requested: Boolean,
    domainBlocking: Boolean,
    showingReblogs: Boolean,
    endorsed: Boolean
  )

  type ReportId = Id[Report]
  case class Report(id: ReportId, actionTaken: String)

  case class Results(accounts: List[Account], statuses: List[Status], hashtags: List[String])

  sealed trait Visibility extends EnumEntry with Lowercase

  object Visibility extends Enum[Visibility] {
    //noinspection TypeAnnotation
    val values = findValues

    case object Public extends Visibility
    case object Unlisted extends Visibility
    case object Private extends Visibility
    case object Direct extends Visibility
  }

  type StatusId = Id[Status]
  case class Status(
    id: StatusId,
    uri: URI,
    url: Option[URL],
    account: Account,
    inReplyToId: Option[StatusId],
    inReplyToAccountId: Option[AccountId],
    reblog: Option[Status],
    content: String,
    createdAt: ZonedDateTime,
    emojis: List[Emoji],
    repliesCount: Int,
    reblogsCount: Int,
    favouritesCount: Int,
    reblogged: Option[Boolean],
    favourited: Option[Boolean],
    muted: Option[Boolean],
    sensitive: Boolean,
    spoilerText: String,
    visibility: Visibility,
    mediaAttachments: List[Attachment],
    mentions: List[Mention],
    tags: List[Tag],
    application: Option[Application],
    language: Option[String],
    pinned: Option[Boolean]
  )

  case class TagUsage(day: Long, uses: Long, accounts: Long)
  case class Tag(name: String, url: URL, history: Option[List[TagUsage]])

}
