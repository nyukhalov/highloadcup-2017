package com.github.nyukhalov.highloadcup.web.actor

import java.util.UUID

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, ReceiveTimeout, SupervisorStrategy}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.web.actor.PerRequest.WithProps
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import spray.json._

import scala.concurrent.Promise
import scala.concurrent.duration._

trait PerRequest extends Actor with JsonSupport with AppLogger {
  import context._

  def r: RequestContext
  def target: ActorRef
  def message: RestRequest
  def p: Promise[RouteResult]

  setReceiveTimeout(2.seconds)
  target ! message

  override def receive: Receive = {
//    case res: RestMessage => complete(OK, res)
    case res: UserWithId => complete(OK, res)
    case res: VisitWithId => complete(OK, res)
    case res: LocationWithId => complete(OK, res)
    case avg: LocAvgRating => complete(OK, avg)
    case uv: UserVisits => complete(OK, uv)

    case SuccessfulOperation => complete(OK, "{}".parseJson)
    case ne: NotExist => complete(NotFound, ne)
    case v: Validation => complete(BadRequest, v)
    case e: Error => complete(InternalServerError, e)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete(m: => ToResponseMarshallable): Unit = {
    val f = r.complete(m)
    f.onComplete(p.complete(_))
    stop(self)
  }

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy() {
      case e =>
        logger.error(s"Child actor was stopped after failure: ${e.getMessage}")
        complete(InternalServerError, Error(e.getMessage))
        Stop
    }
}

object PerRequest {
  case class WithProps(r: RequestContext, props: Props, message: RestRequest, p: Promise[RouteResult]) extends PerRequest {
    lazy val target: ActorRef = context.actorOf(props, "target")
  }
}

trait PerRequestCreator {
  def perRequest(r: RequestContext, props: Props, req: RestRequest, p: Promise[RouteResult])
                (implicit ac: ActorSystem): ActorRef =
    ac.actorOf(Props(classOf[WithProps], r, props, req, p), s"pr-${UUID.randomUUID().toString}")
}
