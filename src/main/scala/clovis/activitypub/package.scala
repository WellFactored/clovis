package clovis
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{Uri, Url}

package object activitypub {
  type UrlString = String Refined Url
  type UriString = String Refined Uri
}
