package com.github.nyukhalov.highloadcup.core

import java.nio.charset.Charset

import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainCodec
import io.circe.parser.decode

class DataLoader(hlServiceJ: HLServiceJ) extends AppLogger with DomainCodec {

  def loadData(pathToData: String): Unit = {
    val workdir = File(pathToData)
    readData(workdir)
  }

  private def readData(workdir: File) = {
    val start = System.currentTimeMillis()
    logger.info("Loading data..")

    val entity2loadPriority = Map("users" -> 1, "locations" -> 2, "visits" -> 3)

    var usersLoaded = 0
    var locationsLoaded = 0
    var visitsLoaded = 0

    workdir.children
      .filter(f => {
        f.name.startsWith("users") ||
        f.name.startsWith("locations") ||
        f.name.startsWith("visits")
      })
      .map(f => {
        val entityName = f.name.split("_")(0)
        (f, entity2loadPriority(entityName), entityName)
      })
      .toList
      .sortBy(_._2)
      .foreach { case (f, _, entity) =>
        logger.debug(s"Read file: $f")

//        f.byteArray
        val content = f.contentAsString(charset = Charset.forName("UTF-8"))

        entity match {
          case "users" =>
            val users = UsersJ.fromJson(content).users
            usersLoaded += users.size()
            hlServiceJ.addUsers(users)

          case "locations" =>
            val locations = LocationsJ.fromJson(content).locations
            locationsLoaded += locations.size()
            hlServiceJ.addLocations(locations)

          case "visits" =>
            val visits = VisitsJ.fromJson(content).visits
            visitsLoaded += visits.size()
            hlServiceJ.addVisits(visits)

          case t => logger.error(s"Unknown type of data: $t")
        }
      }
    logger.info(s"Data loaded successfully (${System.currentTimeMillis() - start} ms): users=$usersLoaded, locations=$locationsLoaded, visits=$visitsLoaded")
  }
}
