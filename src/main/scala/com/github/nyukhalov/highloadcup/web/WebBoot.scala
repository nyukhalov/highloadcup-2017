package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor.LoadData
import com.github.nyukhalov.highloadcup.core.repository.EntityRepositoryImpl
import com.github.nyukhalov.highloadcup.database.DB
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object WebBoot {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val config = ConfigFactory.load()

    val entityRepository = new EntityRepositoryImpl()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    DB.init().onComplete {
      case Success(_) =>
        actorSystem.actorOf(DataLoaderActor.props(entityRepository), "data-loader") ! LoadData(pathToZip)
        new WebServer(serverPort, entityRepository).start()

      case Failure(ex) => throw new RuntimeException("Failed to init database")
    }
  }
}
