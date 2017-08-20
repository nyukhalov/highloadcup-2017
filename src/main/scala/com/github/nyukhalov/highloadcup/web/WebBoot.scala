package com.github.nyukhalov.highloadcup.web

import java.util.concurrent.{Executors, TimeUnit}

import better.files.File
import com.github.nyukhalov.highloadcup.core.{AppLogger, DataLoader, HLServiceImpl}
import com.typesafe.config.ConfigFactory
import org.rapidoid.http.{HTTP, HttpClient}

import scala.concurrent.duration._

object WebBoot extends AppLogger {
  def run(): Unit = {
    val config = ConfigFactory.load()

    val pathToZip = config.getString("datazip.path")
    val pathToOption = config.getString("option.path")
    val serverPort = config.getInt("server.port")

    val hlService = new HLServiceImpl()
    new DataLoader(hlService).loadData(pathToZip)
    val warmupTime = getWarmupTime(pathToOption)

    val server = new RapidoidHttpServer(serverPort, hlService)
    server.start()

    val s = System.currentTimeMillis()

    hlService.userMap.foreach{ case (id, user) => {
      server.warmupCache(s"/users/$id", user)
    }}
    hlService.locMap.foreach{ case (id, loc) => {
      server.warmupCache(s"/locations/$id", loc)
    }}
    hlService.visitMap.foreach { case (id, visit) => {
      server.warmupCache(s"/visits/$id", visit)
    }}
    logger.info(s"Warmed up for ${System.currentTimeMillis() - s} ms")
  }

  private def getWarmupTime(pathToOption: String) = {
    val TRAIN_TIME = 30.seconds
    val FULL_TIME = 3.minutes

    val option = File(pathToOption)
    if (!option.exists) {
      logger.warn("Can not find option file $pathToOption. User train time")
      TRAIN_TIME
    } else {
      val mode = option.lines().toList(1).toInt
      if (mode == 1) {
        logger.info(s"FULL mode ($FULL_TIME warmup)")
        FULL_TIME
      } else {
        logger.info(s"TRAIN mode ($TRAIN_TIME warmup)")
        TRAIN_TIME
      }
    }
  }
}
