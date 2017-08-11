package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor
import com.github.nyukhalov.highloadcup.core.actor.DataLoaderActor.LoadData
import com.github.nyukhalov.highloadcup.core.repository.EntityRepositoryImpl

import scala.concurrent.ExecutionContext

object WebBoot {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val entityRepository = new EntityRepositoryImpl()

    actorSystem.actorOf(DataLoaderActor.props(entityRepository), "data-loader") ! LoadData("/tmp/data/data.zip")

    new WebServer(entityRepository).start()
  }
}
