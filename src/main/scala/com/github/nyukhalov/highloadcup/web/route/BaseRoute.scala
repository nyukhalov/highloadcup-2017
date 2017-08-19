package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLService}
import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import com.github.nyukhalov.highloadcup.web.domain.{LocAvgRating, NotExist, SuccessfulOperation, UserVisits, Validation}
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import io.circe.Json
import io.circe.parser.decode

trait BaseRoute extends JsonSupport with AppLogger {
  private val EmptyJson = decode[Json]("{}").toOption.get
  private val SO = t123(StatusCodes.OK, EmptyJson)
  private val NE = t123(StatusCodes.NotFound, EmptyJson)
  private val VA = t123(StatusCodes.BadRequest, EmptyJson)

  def hlService: HLService

  private def t123(m: => ToResponseMarshallable): ToResponseMarshallable = {
    m
  }

  def t(o: AnyRef): ToResponseMarshallable = {
    o match {
      case res: User => t123(StatusCodes.OK, res)
      case res: Visit => t123(StatusCodes.OK, res)
      case res: Location => t123(StatusCodes.OK, res)
      case avg: LocAvgRating => t123(StatusCodes.OK, avg)
      case uv: UserVisits => t123(StatusCodes.OK, uv)

      case SuccessfulOperation => SO
      case NotExist => NE
      case Validation => VA
    }
  }
}
