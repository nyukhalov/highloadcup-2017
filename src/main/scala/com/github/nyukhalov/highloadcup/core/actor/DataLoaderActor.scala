package com.github.nyukhalov.highloadcup.core.actor

import java.nio.charset.Charset

import akka.actor.{Actor, Props}
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLService}
import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainJsonProtocol
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, duration}

object DataLoaderActor {

  final case class LoadData(pathToZip: String)

  def props(hLService: HLService): Props = Props(classOf[DataLoaderActor], hLService)
}

class DataLoaderActor(hlService: HLService) extends Actor with AppLogger with DomainJsonProtocol {

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
          val users = content.parseJson.convertTo[Users]
          usersLoaded += users.users.length
          val f = hlService.addUsers(users.users)
          wait(f)

        case "locations" =>
          val locations = content.parseJson.convertTo[Locations]
          locationsLoaded += locations.locations.length
          val f = hlService.addLocations(locations.locations)
          wait(f)


        case "visits" =>
          val visits = content.parseJson.convertTo[Visits]
          visitsLoaded += visits.visits.length
          val f = hlService.addVisits(visits.visits)
          wait(f)

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
