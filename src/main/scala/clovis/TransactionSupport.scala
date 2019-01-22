package clovis
import cats.effect.{Async, ContextShift}
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.syntax.connectionio._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

trait TransactionSupport {
  type ConnectionType[A] = ConnectionIO[A]

  private def xa[F[_]: Async: ContextShift](dbConfig: DBConfig): Aux[F, Unit] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver", // driver classname
    dbConfig.url,
    dbConfig.username,
    dbConfig.password.value
  )

  def tx[F[_]: Async: ContextShift](dbConfig: DBConfig): ConnectionIO ~> F = new ~>[ConnectionIO, F] {
    override def apply[A](fa: ConnectionIO[A]): F[A] =
      fa.transact(xa(dbConfig))
  }
}
