package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import com.github.nyukhalov.highloadcup.core.{AppLogger, DataLoader, HLServiceImpl}
import com.typesafe.config.ConfigFactory

object WebBoot extends AppLogger {
  def run(): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    val hlService = new HLServiceImpl()
    new DataLoader(hlService).loadData(pathToZip)

//    val httpServer: HttpServer = new WebServer(serverPort, hlService)
    val httpServer: HttpServer = new RapidoidHttpServer(serverPort, hlService)

    httpServer.start()

    implicit val actorSystem = ActorSystem("ads")
    implicit val materializer = ActorMaterializer()
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
