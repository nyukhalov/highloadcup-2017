package com.github.nyukhalov.highloadcup.web

import java.util.concurrent.ConcurrentHashMap

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLService}
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import io.circe.parser._
import org.rapidoid.buffer.Buf
import org.rapidoid.bytes.BytesUtil
import org.rapidoid.data.JSON
import org.rapidoid.http._
import org.rapidoid.http.impl.PathPattern
import org.rapidoid.net.abstracts.Channel
import org.rapidoid.net.impl.RapidoidHelper

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class RapidoidHttpServer(serverPort: Int, hlService: HLService)
  extends AbstractHttpServer with JsonSupport with HttpServer with AppLogger {
  private val HTTP_400 = fullResp(400, "nf".getBytes())

  private val EMPTY_JSON = "{}".getBytes()

  private val supportedEntities = Set("users", "locations", "visits")
  private val EmptyParams = mutable.Map[String, String]()

  private val entityMethodPattern = PathPattern.from("/{entity}/{id}/{method}")
  private val entityPattern = PathPattern.from("/{entity}/{id}")

  private val cache: mutable.Map[String, Array[Byte]] = new ConcurrentHashMap[String, Array[Byte]]().asScala

  override def start(): Unit = {
    listen(serverPort)
  }

  private def isKnownEntity(entity: String) = supportedEntities.contains(entity)

  private def parseId(id: String): Option[Int] = Try {
    id.toInt
  } toOption

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
            decode[User](body) match {
              case Left(_) => Validation
              case Right(user) => hlService.createUser(user)
            }
          }

        case ("visits") =>
          if (req.isGet.value) NotExist
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[Visit](body) match {
              case Left(_) => Validation
              case Right(visit) => hlService.createVisit(visit)
            }
          }

        case ("locations") =>
          if (req.isGet.value) NotExist
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[Location](body) match {
              case Left(_) => Validation
              case Right(loc) => hlService.createLocation(loc)
            }
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
          if (req.isGet.value) hlService.getUser(id)
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[UserUpdate](body) match {
              case Left(_) => Validation
              case Right(userUpdate) => {
                hlService.updateUser(id, userUpdate) match {
                  case SuccessfulOperation =>
                    cache.remove(s"/users/$id")
                    SuccessfulOperation

                  case another => another
                }
              }
            }
          }

        case "visits" =>
          if (req.isGet.value) hlService.getVisit(id)
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[VisitUpdate](body) match {
              case Left(_) => Validation
              case Right(visitUpdate) => {
                hlService.updateVisit(id, visitUpdate) match {
                  case SuccessfulOperation =>
                    cache.remove(s"/visits/$id")
                    SuccessfulOperation

                  case another => another
                }
              }
            }
          }

        case "locations" =>
          if (req.isGet.value) hlService.getLocation(id)
          else {
            flushCache = true
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[LocationUpdate](body) match {
              case Left(_) => Validation
              case Right(locUpdate) => {
                hlService.updateLocation(id, locUpdate) match {
                  case SuccessfulOperation =>
                    cache.remove(s"/locations/$id")
                    SuccessfulOperation

                  case another => another
                }
              }
            }
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
              val fromDate = params.get("fromDate").map(s => s.toLong)
              val toDate = params.get("toDate").map(s => s.toLong)
              val country = params.get("country")
              val toDistance = params.get("toDistance").map(s => s.toInt)

              hlService.getUserVisits(id, fromDate, toDate, country, toDistance)
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

              hlService.getAverageRating(id, fromDate, toDate, fromAge, toAge, gender)
            } catch {
              case _: NumberFormatException => Validation
            }
          }

        case _ => NotExist
      }
    }
    toResponse(uri, ctx, req, resp)
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
    val uri = BytesUtil.get(buf.bytes(), req.uri)

    if (req.isGet.value) {
      req.isKeepAlive.value = true
    }

    if (req.isGet.value) {
      val resp = cache.get(uri)
      if (resp.isDefined) {
        json(ctx, req.isKeepAlive.value, resp.get)
      } else {
        handleReq(uri, ctx, buf, req)
      }
    } else {
      handleReq(uri, ctx, buf, req)
    }
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
