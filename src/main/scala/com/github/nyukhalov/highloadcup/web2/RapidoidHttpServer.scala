package com.github.nyukhalov.highloadcup.web2

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import com.github.nyukhalov.highloadcup.core.{DataLoader, HLService, HLServiceImpl}
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import com.typesafe.config.ConfigFactory
import org.rapidoid.buffer.Buf
import org.rapidoid.bytes.BytesUtil
import org.rapidoid.data.JSON
import org.rapidoid.http._
import org.rapidoid.http.impl.PathPattern
import org.rapidoid.net.abstracts.Channel
import org.rapidoid.net.impl.RapidoidHelper
import io.circe.parser._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

object Application extends App {
  val config = ConfigFactory.load()

  val pathToZip = config.getString("datazip.path")
  val serverPort = config.getInt("server.port")

  val hlService = new HLServiceImpl()
  new DataLoader(hlService).loadData(pathToZip)
  new RapidoidHttpServer(hlService).listen(serverPort)
}

class RapidoidHttpServer(hlService: HLService) extends AbstractHttpServer with JsonSupport {
  private val HTTP_400 = fullResp(400, "Not Found".getBytes())

  private val EMPTY_JSON = "{}".getBytes()

  private val supportedEntities = Set("users", "locations", "visits")

  private val entityMethodPattern = PathPattern.from("/{entity}/{id}/{method}")
  private val entityPattern = PathPattern.from("/{entity}/{id}")

  private def isKnownEntity(entity: String) = supportedEntities.contains(entity)

  private def parseId(id: String): Option[Int] = Try {
    id.toInt
  } toOption

  private def badRequest(ctx: Channel, req: RapidoidHelper) = {
    ctx.write(HTTP_400)
    ctx.closeIf(!req.isKeepAlive.value)
    HttpStatus.ASYNC
  }

  private def handleEntityRequest(ctx: Channel, buf: Buf, req: RapidoidHelper, params: mutable.Map[String, String]) = {
    val entity = params("entity")
    val strId = params("id")
    val idOpt = parseId(strId)

    val resp = if (idOpt.isEmpty) {
      Validation
    } else if (!isKnownEntity(entity)) {
      NotExist
    } else {
      val id = idOpt.get
      entity match {
        case "users" =>
          if (req.isGet.value) hlService.getUser(id)
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[UserUpdate](body) match {
              case Left(_) => Validation
              case Right(userUpdate) => hlService.updateUser(id, userUpdate)
            }
          }

        case "visits" =>
          if (req.isGet.value) hlService.getVisit(id)
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[VisitUpdate](body) match {
              case Left(_) => Validation
              case Right(visitUpdate) => hlService.updateVisit(id, visitUpdate)
            }
          }

        case "locations" =>
          if (req.isGet.value) hlService.getLocation(id)
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[LocationUpdate](body) match {
              case Left(_) => Validation
              case Right(locUpdate) => hlService.updateLocation(id, locUpdate)
            }
          }
      }
    }

    toResponse(ctx, req, resp)
  }

  private def handleEntityMethodRequest(ctx: Channel, buf: Buf, req: RapidoidHelper, params: mutable.Map[String, String]) = {
    val method = params("method")
    val entity = params("entity")
    val strId = params("id")
    val idOpt = parseId(strId)

    val resp = if (idOpt.isEmpty) {
      Validation
    } else if (!isKnownEntity(entity)) {
      NotExist
    } else {
      (entity, method) match {
        case ("users", "new") =>
          if (req.isGet.value) NotExist
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[User](body) match {
              case Left(_) => Validation
              case Right(user) => hlService.createUser(user)
            }
          }

        case ("visits", "new") =>
          if (req.isGet.value) NotExist
          else {
            val body = BytesUtil.get(buf.bytes(), req.body)
            decode[Visit](body) match {
              case Left(_) => Validation
              case Right(visit) => hlService.createVisit(visit)
            }
          }

        case ("locations", "new") =>
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
    }
    toResponse(ctx, req, resp)
  }

  private def toResponse(ctx: Channel, req: RapidoidHelper, resp: Any): HttpStatus = {
    resp match {
      case NotExist => HttpStatus.NOT_FOUND
      case Validation => badRequest(ctx, req)
      case SuccessfulOperation => ok(ctx, req.isKeepAlive.value, EMPTY_JSON, MediaType.APPLICATION_JSON)
      case x => {
        serializeToJson(HttpUtils.noReq(), ctx, req.isKeepAlive.value, x)
      }
    }
  }

  override def handle(ctx: Channel, buf: Buf, req: RapidoidHelper): HttpStatus = {

    val path = BytesUtil.get(buf.bytes(), req.path)

    var params = Option(entityPattern.`match`(path) asScala)
    if (params.isDefined) {
      handleEntityRequest(ctx, buf, req, params.get)
    } else {
      params = Option(entityMethodPattern.`match`(path) asScala)
      if (params.isDefined) {
        handleEntityMethodRequest(ctx, buf, req, params.get)
      } else {
        HttpStatus.NOT_FOUND
      }
    }
  }
}
