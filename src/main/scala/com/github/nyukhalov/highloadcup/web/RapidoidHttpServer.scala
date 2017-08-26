package com.github.nyukhalov.highloadcup.web

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import com.github.nyukhalov.highloadcup.CustomServerBuilder
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLServiceJ}
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import org.rapidoid.buffer.Buf
import org.rapidoid.bytes.BytesUtil
import org.rapidoid.data.JSON
import org.rapidoid.http._
import org.rapidoid.http.impl.PathPattern
import org.rapidoid.net.abstracts.Channel
import org.rapidoid.net.impl.RapidoidHelper

import scala.collection.JavaConverters._
import scala.collection.mutable

class RapidoidHttpServer(serverPort: Int, hlServiceJ: HLServiceJ)
  extends AbstractHttpServer("hlcup-server", "nf", "err", true) with JsonSupport with HttpServer with AppLogger {
  private val HTTP_400 = fullResp(400, "nf".getBytes())

  private val EMPTY_JSON = "{}".getBytes()

  private val supportedEntities = Set("users", "locations", "visits")
  private val EmptyParams = mutable.Map[String, String]()

  private val entityMethodPattern = PathPattern.from("/{entity}/{id}/{method}")
  private val entityPattern = PathPattern.from("/{entity}/{id}")

  private val cache: mutable.Map[String, Array[Byte]] = new ConcurrentHashMap[String, Array[Byte]]().asScala

  private val heavyMethodCachingEnabled = new AtomicBoolean(false)

  override def start(): Unit = {
//    listen(serverPort)
    listen()
  }

  private def listen() = {
    val syncBufs = false // default true
    val noDelay = true // default false
    val blockingAccept = false // default false

    val workersCount = Math.max(1, Runtime.getRuntime.availableProcessors() - 1)

    val builder = new CustomServerBuilder()
      .blockingAccept(blockingAccept)
      .protocol(this)
      .address("0.0.0.0")
      .port(serverPort)
      .syncBufs(syncBufs)
      .workers(workersCount)

    builder.noDelay(noDelay)

    builder
      .build
      .start
  }

  private def isKnownEntity(entity: String) = supportedEntities.contains(entity)

  private def isInt(value: String): Boolean = {
    val len = value.length

    (0 until len).foreach { i =>
      val c = value.charAt(i)
      if (i == 0 && c == '-') {}
      else {
        val d = Character.digit(c, 10)
        if (d < 0) return false
      }
    }

    true
  }

  private def parseInt(value: String): Option[Int] = {
    if (isInt(value)) Some(value.toInt)
    else None
  }

  private def parseId(id: String): Option[Int] = parseInt(id)

  private def badRequest(ctx: Channel, req: RapidoidHelper) = {
    ctx.write(HTTP_400)
    ctx.closeIf(!req.isKeepAlive.value)
    HttpStatus.ASYNC
  }

  private def handleEntityRequest(uri: String, ctx: Channel, buf: Buf, req: RapidoidHelper, params: mutable.Map[String, String]) = {
    val entity = params("entity")
    val strId = params("id")
    val idOpt = parseId(strId)
    var flushCache = false

    val resp = if (strId == "new") {
      entity match {
        case ("users") =>
          if (req.isGet.value) NotExist
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            val user = UserJ.fromJson(body)
            if (user != null  && user.isValid) {
              if (hlServiceJ.createUser(user)) SuccessfulOperation
              else Validation
            }
            else Validation
          }

        case ("visits") =>
          if (req.isGet.value) NotExist
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            val visit = VisitJ.fromJson(body)
            if (visit != null && visit.isValid) hlServiceJ.createVisit(visit)
            else Validation
          }

        case ("locations") =>
          if (req.isGet.value) NotExist
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            val loc = LocationJ.fromJson(body)
            if (loc != null && loc.isValid) {
              if (hlServiceJ.createLocation(loc)) SuccessfulOperation
              else Validation
            }
            else Validation
          }

        case _ => NotExist
      }
    } else if (idOpt.isEmpty) {
      NotExist
    } else if (!isKnownEntity(entity)) {
      NotExist
    } else {
      val id = idOpt.get
      entity match {
        case "users" =>
          if (req.isGet.value) {
            val u = hlServiceJ.getUser(id)
            if (u == null) NotExist
            else u
          }
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            val uuj = UserUpdateJ.fromJson(body)
            if (uuj != null && uuj.isValid) {
              hlServiceJ.updateUser(id, uuj) match {
                case SuccessfulOperation =>
                  cache.remove(s"/users/$id")
                  SuccessfulOperation

                case another => another
              }
            }
            else Validation
          }

        case "visits" =>
          if (req.isGet.value) {
            val v = hlServiceJ.getVisit(id)
            if (v == null) NotExist
            else v
          }
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            val vuj = VisitUpdateJ.fromJson(body)
            if (vuj != null && vuj.isValid) {
              hlServiceJ.updateVisit(id, vuj) match {
                case SuccessfulOperation =>
                  cache.remove(s"/visits/$id")
                  SuccessfulOperation

                case another => another
              }
            }
            else Validation
          }

        case "locations" =>
          if (req.isGet.value) {
            val loc = hlServiceJ.getLocation(id)
            if (loc == null) NotExist
            else loc
          }
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            val luj = LocationUpdateJ.fromJson(body)
            if (luj != null && luj.isValid) {
              hlServiceJ.updateLocation(id, luj) match {
                case SuccessfulOperation =>
                  cache.remove(s"/locations/$id")
                  SuccessfulOperation

                case another => another
              }
            }
            else Validation
          }
      }
    }

    toResponse(uri, ctx, req, resp, flushCache)
  }

  private def getParams(buf: Buf, req: RapidoidHelper) = {
    val q = BytesUtil.get(buf.bytes(), req.query)
    if (q.isEmpty) EmptyParams
    else {
      val m = mutable.Map[String, String]()
      val decoded = java.net.URLDecoder.decode(q, "UTF-8")
      decoded.split("&").foreach(kv => {
        val kvs = kv.split("=")
        m += (kvs(0) -> kvs(1))
      })
      m
    }
  }

  private def handleEntityMethodRequest(uri: String, ctx: Channel, buf: Buf, req: RapidoidHelper, params: mutable.Map[String, String]) = {
    val method = params("method")
    val entity = params("entity")
    val strId = params("id")
    val idOpt = parseId(strId)
    var cacheIt = false

    val resp = if (idOpt.isEmpty) {
      Validation
    } else if (!isKnownEntity(entity)) {
      NotExist
    } else {
      val id = idOpt.get
      (entity, method) match {
        case ("users", "visits") =>
          if (!req.isGet.value) NotExist
          else {
            val params = getParams(buf, req)

            try {
              val fromDate: Option[Long] = params.get("fromDate").map(s => s.toLong)
              val toDate: Option[Long] = params.get("toDate").map(s => s.toLong)
              val country: Option[String] = params.get("country")
              val toDistance: Option[Int] = params.get("toDistance").map(s => s.toInt)

              hlServiceJ.getUserVisits(id, fromDate.orNull, toDate.orNull, country.orNull, toDistance.orNull) match {
                case uv: UserVisits =>
                  cacheIt = heavyMethodCachingEnabled.get()
                  uv

                case another => another
              }
            } catch {
              case _: NumberFormatException => Validation
            }
          }

        case ("locations", "avg") =>
          if (!req.isGet.value) NotExist
          else {
            val params = getParams(buf, req)

            try {
              val fromDate = params.get("fromDate").map(s => s.toLong)
              val toDate = params.get("toDate").map(s => s.toLong)
              val fromAge = params.get("fromAge").map(s => s.toInt)
              val toAge = params.get("toAge").map(s => s.toInt)
              val gender = params.get("gender")

              hlServiceJ.getLocAvgRating(id, fromDate.orNull, toDate.orNull, fromAge.orNull, toAge.orNull, gender.orNull) match {
                case avg: LocAvgRating =>
                  cacheIt = heavyMethodCachingEnabled.get()
                  avg

                case another => another
              }
            } catch {
              case _: NumberFormatException => Validation
            }
          }

        case _ => NotExist
      }
    }
    toResponse(uri, ctx, req, resp, cacheIt)
  }

  private def toResponse(uri: String, ctx: Channel, req: RapidoidHelper, resp: Any, cacheIt: Boolean = false): HttpStatus = {
    resp match {
      case NotExist => HttpStatus.NOT_FOUND
      case Validation => badRequest(ctx, req)
      case SuccessfulOperation => ok(ctx, req.isKeepAlive.value, EMPTY_JSON, MediaType.APPLICATION_JSON)
      case x => {
        val bytes = JSON.stringifyToBytes(x)
        if (cacheIt && req.isGet.value) {
          cache += (uri -> bytes)
        }
        json(ctx, req.isKeepAlive.value, bytes)
      }
    }
  }

  override def handle(ctx: Channel, buf: Buf, req: RapidoidHelper): HttpStatus = {
//    val s = System.nanoTime()

    if (!req.isGet.value && !heavyMethodCachingEnabled.get()) {
      logger.info("Enable caching of heavy methods")
      // предполагается что после ПОСТ фазы можно кэшировать сложные запросы
      heavyMethodCachingEnabled.set(true)
    }

    val uri = BytesUtil.get(buf.bytes(), req.uri)

    val res = if (req.isGet.value) {
      val resp = cache.get(uri)
      if (resp.isDefined) {
        json(ctx, req.isKeepAlive.value, resp.get)
      } else {
        handleReq(uri, ctx, buf, req)
      }
    } else {
      handleReq(uri, ctx, buf, req)
    }

//    logger.info(s"$uri processed for ${System.nanoTime() - s}")

    res
  }

  private def handleReq(uri: String, ctx: Channel, buf: Buf, req: RapidoidHelper): HttpStatus = {
    val path = BytesUtil.get(buf.bytes(), req.path)

    var params = Option(entityPattern.`match`(path).asScala)
    if (params.isDefined) {
      handleEntityRequest(uri, ctx, buf, req, params.get)
    } else {
      params = Option(entityMethodPattern.`match`(path).asScala)
      if (params.isDefined) {
        handleEntityMethodRequest(uri, ctx, buf, req, params.get)
      } else {
        HttpStatus.NOT_FOUND
      }
    }
  }

  def warmupCache(uri: String, responseObj: Any): Unit = {
    cache += (uri -> JSON.stringifyToBytes(responseObj))
  }
}
