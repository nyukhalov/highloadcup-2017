package com.github.nyukhalov.highloadcup.web2

import java.lang.Character.{ LETTER_NUMBER => CR,LINE_SEPARATOR => LF }
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup._
import java.nio.channels.AsynchronousServerSocketChannel._
import java.nio.channels.{ AsynchronousSocketChannel => ASC }
import java.nio.channels.CompletionHandler
import java.nio.ByteBuffer._
import java.util.concurrent.Executors._
import scala.annotation.implicitNotFound
import scala.collection.mutable.ListBuffer

object HttpServer extends App {
  val channelGroup = withFixedThreadPool(1, defaultThreadFactory())
  val listenChannel = open(channelGroup).bind(new InetSocketAddress(8000))
  while (true) MyHttpProcess(listenChannel.accept().get())()
}

case class MyHttpProcess(ch: ASC) {
  val buf = allocate(1024)
  buf.flip()

  implicit def fn2CH(fn: Int => Unit) = new CompletionHandler[Integer, Void]() {
    def completed(res: Integer, _2: Void): Unit = fn(res)
    def failed(_1: Throwable, _2: Void) = throw new RuntimeException(_1)
  }

  def apply() = readLine { request_line =>
    readHeader { header =>
      ch.write(wrap(html), null, (res: Int) => ch.close())
    }
  }

  def readHeader(fn: List[String] => Unit, lines: ListBuffer[String] = new ListBuffer): Unit = readLine {
    case "" => fn(lines.toList)
    case any => readHeader(fn, lines += any);
  }

  def readLine(fn: String => Unit, sb: StringBuilder = new StringBuilder): Unit = readChar {
    case LF => readChar { case CR => fn(sb.toString) }
    case CR => fn(sb.toString);
    case any => readLine(fn, sb += any)
  }

  def readChar(fn: Char => Unit) = buf.hasRemaining() match {
    case true => fn(buf.get().toChar);
    case _ =>
      buf.clear()
      ch.read(buf, null, (res: Int) => if (res != -1) { buf.flip(); fn(buf.get().toChar) } else ch.close())
  }

  val html = """
HTTP/1.1 200
Client
Date: Tue, 24 Apr 2012 10:10:51 GMT
Content-Type: text/html; charset=ISO-8859-1
<h1>Hello World</h2>""".getBytes
}
