package clovis
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url

package object activitypub {
type UrlString = String Refined Url
}
