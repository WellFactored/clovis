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
