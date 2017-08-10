package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, ReceiveTimeout}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.github.nyukhalov.highloadcup.web.actor.PerRequest.{WithActorRef, WithProps}
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

import scala.concurrent.Promise
import scala.concurrent.duration._

trait PerRequest extends Actor with JsonSupport {
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
    case v: Validation => complete(BadRequest, v)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete(m: => ToResponseMarshallable) = {
    val f = r.complete(m)
    f.onComplete(p.complete(_))
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e =>
        complete(InternalServerError, Error(e.getMessage))
        Stop
    }
}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestRequest, p: Promise[RouteResult]) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestRequest, p: Promise[RouteResult]) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  def perRequest(r: RequestContext, target: ActorRef, req: RestRequest, p: Promise[RouteResult])
                (implicit ac: ActorSystem): ActorRef =
    ac.actorOf(Props(new WithActorRef(r, target, req, p)))

  def perRequest(r: RequestContext, props: Props, req: RestRequest, p: Promise[RouteResult])
                (implicit ac: ActorSystem): ActorRef =
    ac.actorOf(Props(new WithProps(r, props, req, p)))
}
