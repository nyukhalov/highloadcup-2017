package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor.LoadData
import com.github.nyukhalov.highloadcup.database.DB
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object WebBoot {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    DB.init().onComplete {
      case Success(_) =>
        actorSystem.actorOf(DataLoaderActor.props(), "data-loader") ! LoadData(pathToZip)
        new WebServer(serverPort).start()

        val http = Http()
        http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1"))
        http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1"))
        http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/visits/1"))
        http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1/visits"))
        http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1/avg"))

      case Failure(ex) => throw new RuntimeException("Failed to init database")
    }
  }
}
