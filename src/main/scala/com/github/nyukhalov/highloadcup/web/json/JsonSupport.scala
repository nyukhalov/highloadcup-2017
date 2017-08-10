package com.github.nyukhalov.highloadcup.web.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.nyukhalov.highloadcup.core.domain.User
import spray.json.{DefaultJsonProtocol, JsValue, JsonWriter, RootJsonFormat}
import com.github.nyukhalov.highloadcup.web.domain._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import reflect._

  private val PASS1 = """([A-Z]+)([A-Z][a-z])""".r
  private val PASS2 = """([a-z\d])([A-Z])""".r
  private val REPLACEMENT = "$1_$2"

  implicit val userFormat = jsonFormat6(User)

  // model
  implicit val errorFormat = jsonFormat1(Error)
  implicit val validationFormat = jsonFormat1(Validation)

  // rest messages (responses)
  implicit object UserWithIdFormat extends RootJsonFormat[UserWithId] {
    override def read(json: JsValue): UserWithId = {
      throw new RuntimeException("Not implemented")
    }

    override def write(obj: UserWithId): JsValue = userFormat.write(obj.user)
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

  /**
    * This is the most important piece of code in this object!
    * It overrides the default naming scheme used by spray-json and replaces it with a scheme that turns camelcased
    * names into snakified names (i.e. using underscores as word separators).
    */
  override protected def extractFieldNames(classTag: ClassTag[_]) = {
    import java.util.Locale

    def snakify(name: String) = PASS2.replaceAllIn(PASS1.replaceAllIn(name, REPLACEMENT), REPLACEMENT).toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map { snakify(_) }
  }
}
