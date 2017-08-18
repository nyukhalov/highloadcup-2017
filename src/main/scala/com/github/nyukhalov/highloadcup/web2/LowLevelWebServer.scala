package com.github.nyukhalov.highloadcup.web2

import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors

class LowLevelWebServer(port: Int, poolSize: Int) extends Runnable {
  val serverSocket = new ServerSocket(port)
  val executorService = Executors.newFixedThreadPool(poolSize)

  override def run() = {
    try {
      while (true) {
        val socket = serverSocket.accept()
        executorService.execute(new Handler(socket))
      }
    } finally {
      executorService.shutdown()
    }
  }
}

class Handler(socket: Socket) extends Runnable {
  def message = (Thread.currentThread.getName + "\n").getBytes

  override def run() {
    socket.getOutputStream.write(message)
    socket.getOutputStream.close()
  }
}

object Boot extends App {
  val cpu = Runtime.getRuntime.availableProcessors()
  new LowLevelWebServer(8080, cpu).run()
}
