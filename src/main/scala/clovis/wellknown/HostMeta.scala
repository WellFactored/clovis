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

package clovis.wellknown

import java.net.URI

import scala.xml.Elem

case class Link(
  rel:        String,
  `type`:     Option[String],
  href:       Option[URI],
  titles:     Option[Map[String, String]],
  properties: Option[Map[URI, Option[String]]],
  template:   Option[String])



case class HostMeta(links: Seq[Link]) {
  lazy val toXML: Elem =
    <XRD xmlns="http://docs.oasis-open.org/ns/xri/xrd-1.0">
      {linksAsXML(links)}
    </XRD>

  private def linksAsXML(links: Seq[Link]): Seq[Elem] =
    links.map(link => <Link rel="lrdd" type="application/xrd+xml" template={link.template.getOrElse("")}/>)
}
