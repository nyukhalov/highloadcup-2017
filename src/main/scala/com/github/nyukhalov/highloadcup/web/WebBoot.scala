package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.{AppLogger, DataLoader, HLServiceImpl}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object WebBoot extends AppLogger {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    val hlService = new HLServiceImpl()
    new DataLoader(hlService).loadData(pathToZip)
    new WebServer(serverPort, hlService).start()

    val http = Http()
    (1 to 5).foreach { _ =>
      http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1"))
      http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1"))
      http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/visits/1"))
      http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/users/1/visits"))
      http.singleRequest(HttpRequest(uri = s"http://localhost:$serverPort/locations/1/avg"))

      val emptyJson = HttpEntity(ContentTypes.`application/json`, "{}".getBytes)
      http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"http://localhost:$serverPort/users/1/new", entity = emptyJson))
      http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"http://localhost:$serverPort/visits/1/new", entity = emptyJson))
      http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"http://localhost:$serverPort/locations/1/new", entity = emptyJson))
    }
  }
}
