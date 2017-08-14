package com.github.nyukhalov.highloadcup.core.actor

import java.nio.charset.Charset

import akka.actor.{Actor, Props}
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import better.files._
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.json.DomainJsonProtocol
import spray.json._

object DataLoaderActor {

  final case class LoadData(pathToZip: String)

  def props(entityRepository: EntityRepository): Props = Props(classOf[DataLoaderActor], entityRepository)
}

class DataLoaderActor(entityRepository: EntityRepository) extends Actor with AppLogger with DomainJsonProtocol {

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
    workdir.children.toList.foreach(f => {
      logger.info(s"Read file: $f")

      val content = f.contentAsString(charset = Charset.forName("UTF-8"))

      f.name.split("_")(0) match {
        case "users" =>
          val users = content.parseJson.convertTo[Users]
          users.users.foreach(u => entityRepository.saveUser(u))

        case "locations" =>
          val locations = content.parseJson.convertTo[Locations]
          locations.locations.foreach(l => entityRepository.saveLocation(l))

        case "visits" =>
          val visits = content.parseJson.convertTo[Visits]
          visits.visits.foreach(v => entityRepository.saveVisit(v))

        case t => logger.error(s"Unknown type of data: $t")
      }
    })
    logger.info("Data loaded successfully")
  }

  override def receive: Receive = {
    case LoadData(pathToZip) =>
      val workdir = unzipFiles(pathToZip)
      loadData(workdir)
      workdir.delete(true)
      context.stop(self)
  }
}
