package com.github.nyukhalov.highloadcup.web.json

import com.github.nyukhalov.highloadcup.core.json.{DomainJsonProtocol, LowerCaseJsonProtocol}
import spray.json.{DefaultJsonProtocol, JsNull, JsNumber, JsValue, JsonFormat, JsonWriter, RootJsonFormat, deserializationError}
import com.github.nyukhalov.highloadcup.web.domain._

trait JsonSupport extends LowerCaseJsonProtocol with DomainJsonProtocol {

  implicit object MyFloatJsonFormat extends JsonFormat[Float] {
    def write(x: Float): JsValue = JsNumber(BigDecimal(x).setScale(5, BigDecimal.RoundingMode.HALF_UP))
    def read(value: JsValue): Float = DefaultJsonProtocol.FloatJsonFormat.read(value)
  }

  class MyOptionJsonFormat[T: JsonFormat] extends OptionFormat[T] {
    override def read(value: JsValue): Option[T] = {
      value match {
        case JsNull => deserializationError("Expected not null value")
        case _ => super.read(value)
      }
    }
  }

  override implicit def optionFormat[T: JsonFormat]: JsonFormat[Option[T]] = new MyOptionJsonFormat[T]

  implicit val errorFormat = jsonFormat1(Error)
  implicit val validationFormat = jsonFormat1(Validation)

  implicit val notExistFormat = jsonFormat1(NotExist)
  implicit val userUpdateFormat = jsonFormat5(UserUpdate)
  implicit val visitUpdateFormat = jsonFormat4(VisitUpdate)

  implicit val locationUpdateFormat = jsonFormat4(LocationUpdate)

  implicit val locAvgRating = jsonFormat1(LocAvgRating)
  implicit val userVisitsFormat = jsonFormat3(UserVisit)
  implicit val userVisitFormat = jsonFormat1(UserVisits)

  // rest messages (responses)
  implicit object UserWithIdFormat extends RootJsonFormat[UserWithId] {
    override def read(json: JsValue): UserWithId = {
      throw new RuntimeException("Not implemented")
    }
    override def write(obj: UserWithId): JsValue = userFormat.write(obj.user)
  }
  implicit object VisitWithIdFormat extends RootJsonFormat[VisitWithId] {
    override def read(json: JsValue): VisitWithId = {
      throw new RuntimeException("Not implemented")
    }
    override def write(obj: VisitWithId): JsValue = visitFormat.write(obj.visit)
  }
  implicit object LocationWithIdFormat extends RootJsonFormat[LocationWithId] {
    override def read(json: JsValue): LocationWithId = {
      throw new RuntimeException("Not implemented")
    }
    override def write(obj: LocationWithId): JsValue = locationFormat.write(obj.location)
  }
//  implicit object RestMessageFormat extends RootJsonFormat[RestMessage] {
//    implicit def go[T <: RestMessage](rm: T)(implicit jw: JsonWriter[T]): JsValue = {
//      jw.write(rm)
//    }
//
//    override def write(obj: RestMessage): JsValue = go(obj)
//
//    override def read(json: JsValue): RestMessage = {
//      throw new RuntimeException("Not implemented")
//    }
//  }
}
