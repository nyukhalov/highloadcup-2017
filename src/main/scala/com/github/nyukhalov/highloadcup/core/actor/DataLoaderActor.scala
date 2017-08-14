package com.github.nyukhalov.highloadcup.core.actor

import java.nio.charset.Charset

import akka.actor.{Actor, Props}
import com.github.nyukhalov.highloadcup.core.AppLogger
import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainJsonProtocol
import com.github.nyukhalov.highloadcup.database.DB
import spray.json._
import scala.concurrent.duration._

import scala.concurrent.{Await, Future, duration}

object DataLoaderActor {

  final case class LoadData(pathToZip: String)

  def props(): Props = Props(classOf[DataLoaderActor])
}

class DataLoaderActor() extends Actor with AppLogger with DomainJsonProtocol {

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

    workdir.children.toList.map(x => {
      val entityName = x.name.split("_")(0)
      (x, entity2loadPriority(entityName))
    }).sortBy(_._2).foreach { case (f, _) =>
      logger.info(s"Read file: $f")

      val content = f.contentAsString(charset = Charset.forName("UTF-8"))

      f.name.split("_")(0) match {
        case "users" =>
          val users = content.parseJson.convertTo[Users]
          val f = DB.insertUsers(users.users)
          wait(f)

        case "locations" =>
          val locations = content.parseJson.convertTo[Locations]
          val f = DB.insertLocations(locations.locations)
          wait(f)


        case "visits" =>
          val visits = content.parseJson.convertTo[Visits]
          val f = DB.insertVisits(visits.visits)
          wait(f)

        case t => logger.error(s"Unknown type of data: $t")
      }
    }
    logger.info("Data loaded successfully")
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
