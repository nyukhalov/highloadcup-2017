package com.github.nyukhalov.highloadcup.core.actor

import akka.actor.{Actor, Props}
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository

object DataLoaderActor {
  final case class LoadData(pathToZip: String)

  def props(entityRepository: EntityRepository): Props = Props(classOf[DataLoaderActor], entityRepository)
}

class DataLoaderActor(entityRepository: EntityRepository) extends Actor with AppLogger {
  import DataLoaderActor._

  override def receive: Receive = {
    case LoadData(pathToZip) =>
      logger.info(s"Extract data from $pathToZip")
  }
}
