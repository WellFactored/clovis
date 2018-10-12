package scaladon

import org.http4s.HttpRoutes

/**
  * `HttpService` combines a set of routes with a mount point. These form the parameters to the
  * `BlazeBuilder.mountService`
  */
trait HttpService[F[_]] {
  def routes: HttpRoutes[F]
  def mountPoint: String
}
