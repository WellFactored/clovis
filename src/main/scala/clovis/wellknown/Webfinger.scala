package clovis.wellknown
import java.net.URI

case class Webfinger(subject: URI, aliases: Option[List[URI]], links: Option[List[Link]], properties: Option[Map[URI, Option[String]]])
