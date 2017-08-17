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

  implicit val userUpdateFormat = jsonFormat5(UserUpdate)
  implicit val visitUpdateFormat = jsonFormat4(VisitUpdate)

  implicit val locationUpdateFormat = jsonFormat4(LocationUpdate)

  implicit val locAvgRating = jsonFormat1(LocAvgRating)
  implicit val userVisitsFormat = jsonFormat3(UserVisit)
  implicit val userVisitFormat = jsonFormat1(UserVisits)
}
