/*
 * Copyright (c) 2011-2017 Interfaculty Department of Geoinformatics, University of
 * Salzburg (Z_GIS) & Institute of Geological and Nuclear Sciences Limited (GNS Science)
 * in the SMART Aquifer Characterisation (SAC) programme funded by the New Zealand
 * Ministry of Business, Innovation and Employment (MBIE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.users

import scala.util.Try

sealed trait StatusToken {
  def value: String
}

object StatusToken {

  case object BLOCKED extends StatusToken {
    val value: String = "BLOCKED"
  }

  case object DELETED extends StatusToken {
    val value: String = "DELETED"
  }

  case object REGISTERED extends StatusToken {
    val value: String = "REGISTERED"
  }

  case object ACTIVE extends StatusToken {
    val value: String = "ACTIVE"
  }

  case object PASSWORDRESET extends StatusToken {
    val value: String = "PASSWORDRESET"
  }

  case object EMAILVALIDATION extends StatusToken {
    val value: String = "EMAILVALIDATION"
  }

  def apply(v: String) : StatusToken = {
    v match {
      case "REGISTERED" => REGISTERED
      case "ACTIVE" => ACTIVE
      case "PASSWORDRESET" => PASSWORDRESET
      case "EMAILVALIDATION" => EMAILVALIDATION
      case "BLOCKED" => BLOCKED
      case "DELETED" => DELETED
      case _ => throw new IllegalArgumentException(s"Value $v is not a valid StatusToken value")
    }
  }

  val activatedTokens: List[String] = List(ACTIVE.value, PASSWORDRESET.value, EMAILVALIDATION.value)

  val deactivatedTokens: List[String] = List(BLOCKED.value, DELETED.value)

  def isValid(v: String) : Boolean = {
    Try (apply(v)).isSuccess
  }

  def isActive(token: StatusToken): Boolean = {
    activatedTokens.contains(token.value)
  }

  def isBlocked(token: StatusToken): Boolean = {
    deactivatedTokens.contains(token.value)
  }
}
