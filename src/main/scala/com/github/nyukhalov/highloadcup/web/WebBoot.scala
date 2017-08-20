package com.github.nyukhalov.highloadcup.web

import com.github.nyukhalov.highloadcup.core.{AppLogger, DataLoader, HLServiceImpl}
import com.typesafe.config.ConfigFactory
import org.rapidoid.http.HTTP

object WebBoot extends AppLogger {
  def run(): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val serverPort = config.getInt("server.port")

    val hlService = new HLServiceImpl()
    new DataLoader(hlService).loadData(pathToZip)

    new RapidoidHttpServer(serverPort, hlService).start()

    val c = HTTP.client()
    c.req().get(s"http://localhost:$serverPort/users/1")
    c.req().get(s"http://localhost:$serverPort/visits/1")
    c.req().get(s"http://localhost:$serverPort/users/1")
    c.req().get(s"http://localhost:$serverPort/users/1/visits")
    c.req().get(s"http://localhost:$serverPort/locations/1/avg")
  }
}
