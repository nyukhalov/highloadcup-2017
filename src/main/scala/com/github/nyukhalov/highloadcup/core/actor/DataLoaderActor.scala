package com.github.nyukhalov.highloadcup.core.actor

import java.nio.charset.Charset

import akka.actor.{Actor, Props}
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLService}
import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainCodec
import io.circe._, io.circe.parser._
import io.circe.parser.decode
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, duration}

object DataLoaderActor {

  final case class LoadData(pathToZip: String)

  def props(hLService: HLService): Props = Props(classOf[DataLoaderActor], hLService)
}

class DataLoaderActor(hlService: HLService) extends Actor with AppLogger with DomainCodec {

  import DataLoaderActor._

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
      logger.debug(s"Read file: $f")

      val content = f.contentAsString(charset = Charset.forName("UTF-8"))

      f.name.split("_")(0) match {
        case "users" =>
          decode[Users](content) match {
            case Right(users) =>
              usersLoaded += users.users.length
              val f = hlService.addUsers(users.users)
              wait(f)
          }


        case "locations" =>
          decode[Locations](content) match {
            case Right(locations) =>
              locationsLoaded += locations.locations.length
              val f = hlService.addLocations(locations.locations)
              wait(f)
          }

        case "visits" =>
          decode[Visits](content) match {
            case Right(visits) =>
              visitsLoaded += visits.visits.length
              val f = hlService.addVisits(visits.visits)
              wait(f)
          }

        case t => logger.error(s"Unknown type of data: $t")
      }
    }
    logger.info(s"Data loaded successfully: users=$usersLoaded, locations=$locationsLoaded, visits=$visitsLoaded")
  }

  private def wait[T](future: Future[T]) = {
    implicit val ec = context.dispatcher
    Await.result(future, 30.seconds)
  }

  override def receive: Receive = {
    case LoadData(pathToZip) =>
      val workdir = unzipFiles(pathToZip)
      loadData(workdir)
      workdir.delete(true)
      context.stop(self)
  }
}
