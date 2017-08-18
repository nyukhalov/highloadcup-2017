package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLServiceImpl}
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor.LoadData
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object WebBoot extends AppLogger {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    val hlService = new HLServiceImpl()
    actorSystem.actorOf(DataLoaderActor.props(hlService), "data-loader") ! LoadData(pathToZip)
    new WebServer(serverPort, hlService).start()

    val http = Http()
    http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1"))
    http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1"))
    http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/visits/1"))
    http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1/visits"))
    http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1/avg"))
  }
}
