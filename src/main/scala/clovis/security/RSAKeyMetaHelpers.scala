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

package clovis.security
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}

import doobie.Meta

trait RSAKeyMetaHelpers {
  import RSAKeyCodec._

  implicit val rsaPublicKeyMeta: Meta[RSAPublicKey] =
    Meta[String].imap(decodePublicKey)(encodePublicKey)

  implicit val rsaPrivateKeyMeta: Meta[RSAPrivateKey] =
    Meta[String].imap(decodePrivateKey)(encodePrivateKey)
}
