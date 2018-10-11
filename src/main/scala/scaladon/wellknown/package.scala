package scaladon

import java.net.URI

package object wellknown {

  case class Link(rel: String, `type`: Option[String], href: Option[URI], titles: Option[Map[String, String]] = None, properties: Option[Map[URI, Option[String]]] = None, template: Option[String] = None)
  case class WebfingerResult(subject: URI, aliases: Option[List[URI]], links: Option[List[Link]], properties: Option[Map[URI, Option[String]]])
}
