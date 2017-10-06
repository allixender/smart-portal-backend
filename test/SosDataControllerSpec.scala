/*
 * Copyright (C) 2011-2017 Interfaculty Department of Geoinformatics, University of
 * Salzburg (Z_GIS) & Institute of Geological and Nuclear Sciences Limited (GNS Science)
 * in the SMART Aquifer Characterisation (SAC) programme funded by the New Zealand
 * Ministry of Business, Innovation and Employment (MBIE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import models.sosdata.{SosCapabilities, Wml2Export}
import play.api.mvc.Results

import scala.io.Source

class SosDataControllerSpec extends WithDefaultTest with Results {

  private lazy val capaResource1  = this.getClass().getResource("tvp/sos-200-getcapa.xml")
  private lazy val wml2Resource1 = this.getClass().getResource("tvp/sos-wml2-one-series.xml")
  val url = "https://portal.smart-project.info/sos-smart/service?service=SOS&request=GetCapabilities&AcceptVersions=2.0.0"

  "SosCapabilites Parser" should {

    "read capabilites from xml" in {
      val xml = Source.fromURL(capaResource1)
      val sosCapa = SosCapabilities.fromXml(scala.xml.XML.load(capaResource1), url).get

      sosCapa.featuresOfInterest must contain ("http://vocab.smart-project.info/ngmp/feature/389")
      sosCapa.observedProperties must contain ("http://vocab.smart-project.info/ngmp/phenomenon/1616")
      sosCapa.procedures must contain ("http://vocab.smart-project.info/ngmp/procedure/1616")
      sosCapa.offerings must contain ("http://vocab.smart-project.info/ngmp/offering/1619")
      sosCapa.responseFormats.get must contain ("http://www.opengis.net/om/2.0")
      sosCapa.responseFormats.get must contain ("http://www.opengis.net/waterml/2.0")
      sosCapa.sosUrl mustEqual url
    }

    "read write json" in {
      val xml = Source.fromURL(capaResource1)
      val sosCapa = SosCapabilities.fromXml(scala.xml.XML.load(capaResource1), url).get

      SosCapabilities.fromJson(sosCapa.toJson()).get mustEqual sosCapa
      SosCapabilities.fromJson(sosCapa.toJson()).get.toJson() mustEqual sosCapa.toJson()
    }
  }

  "SosDataController" should {
    "getWml2ExportFromSosGetObs" in {

      val sosCapa = SosCapabilities.fromXml(scala.xml.XML.load(capaResource1), url).get

      val appTimeZone: String = "Pacific/Auckland"
      val wml2Exporter = new Wml2Export(appTimeZone)
      val sourceString = scala.io.Source.fromURL(wml2Resource1).getLines().mkString

      val wml2 = wml2Exporter.getWml2ExportFromSosGetObs(sourceString,sosCapa)

      println(wml2.toString())
    }
  }
  /*
  POST        /api/v1/sos/timeseries                         controllers.SosDataController.getTimeseries()
GET         /api/v1/sos/getCapabilities                    controllers.SosDataController.getCapabilities(sosUrl: String)
   */
}
