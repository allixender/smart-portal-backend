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

package models.db

import java.sql.Connection
import javax.inject.{Inject, Singleton}

import play.api.db.Database
import utils.ClassnameLogger

/**
  * trying higher level db mgmt to unify DAOs better and handle cross-DAO transactions
  *
  * @param database
  */
@Singleton
class SessionHolder @Inject()(database: Database) extends AbstractSessionHolder with ClassnameLogger{

  val db: Database = database

  /**
    * enclose sql blocks with availabe sharable connection
    *
    * @param func
    * @tparam T
    * @return
    */
  def viaConnection[T](func: Connection => T) : T = {
    db.withConnection {
      implicit connection =>
        func(connection)
    }
  }

  /**
    * block to bundle transactional db calls
    *
    * @param func
    * @tparam T
    * @return
    */
  def viaTransaction[T](func: Connection => T) : T = {
    db.withTransaction {
      implicit connection =>
        func(connection)
    }
  }
}
