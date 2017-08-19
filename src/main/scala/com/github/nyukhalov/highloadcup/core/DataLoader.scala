package com.github.nyukhalov.highloadcup.core

import java.nio.charset.Charset

import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainCodec
import io.circe.parser.decode

class DataLoader(hlService: HLService) extends AppLogger with DomainCodec {

  def loadData(pathToZip: String): Unit = {
    val workdir = unzipFiles(pathToZip)
    loadData(workdir)
    workdir.delete(true)
  }

  private def unzipFiles(pathToZip: String) = {
    val workdir = File.newTemporaryDirectory()
    logger.info(s"Extracting data from $pathToZip to directory $workdir")

    val zipFile = File(pathToZip)
    zipFile.unzipTo(destination = workdir)

    //    zipFile.delete(true)
    logger.info("Data extracted successfully..")
    workdir
  }

  private def loadData(workdir: File) = {
    logger.info("Loading data..")

    val entity2loadPriority = Map("users" -> 1, "locations" -> 2, "visits" -> 3)

    var usersLoaded = 0
    var locationsLoaded = 0
    var visitsLoaded = 0

    workdir.children.toList.map(x => {
      val entityName = x.name.split("_")(0)
      (x, entity2loadPriority(entityName))
    }).sortBy(_._2).foreach { case (f, _) =>
      logger.info(s"Read file: $f")

      val content = f.contentAsString(charset = Charset.forName("UTF-8"))

      f.name.split("_")(0) match {
        case "users" =>
          decode[Users](content) match {
            case Right(users) =>
              usersLoaded += users.users.length
              hlService.addUsers(users.users)
          }


        case "locations" =>
          decode[Locations](content) match {
            case Right(locations) =>
              locationsLoaded += locations.locations.length
              hlService.addLocations(locations.locations)
          }

        case "visits" =>
          decode[Visits](content) match {
            case Right(visits) =>
              visitsLoaded += visits.visits.length
              hlService.addVisits(visits.visits)
              visits.visits
          }

        case t => logger.error(s"Unknown type of data: $t")
      }
    }
    logger.info(s"Data loaded successfully: users=$usersLoaded, locations=$locationsLoaded, visits=$visitsLoaded")
  }
}
