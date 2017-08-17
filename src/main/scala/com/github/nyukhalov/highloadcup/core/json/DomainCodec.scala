package com.github.nyukhalov.highloadcup.core.json

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.{MediaTypes, RequestEntity}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers}
import akka.http.scaladsl.util.FastFuture
import cats.Show
import com.github.nyukhalov.highloadcup.core.domain._
import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.Future
import scala.util.control.NoStackTrace

trait DomainCodec {

  implicit val jsonMarshaller: Marshaller[Json, RequestEntity] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(_.noSpaces)

  implicit def jsonTMarshaller[T](implicit en: Encoder[T]) = jsonMarshaller.compose[T](en(_))

  import io.circe.parser._

  implicit val entityUnmarshaller: FromEntityUnmarshaller[Json] =
    PredefinedFromEntityUnmarshallers.
      stringUnmarshaller.
      flatMap(_ => _ => str => xorToFuture(parse(str)))

  implicit def entityTUnmarshaller[T](implicit dec: Decoder[T]): FromEntityUnmarshaller[T] =
    PredefinedFromEntityUnmarshallers
      .stringUnmarshaller
      .flatMap(_ => _ => str => xorToFuture(decode[T](str)))

  private def xorToFuture[A, B](value: Either[A, B])(implicit SA: Show[A]): Future[B] =
    value.fold(
      error => FastFuture.failed(new Throwable(SA.show(error)) with NoStackTrace),
      FastFuture.successful
    )

  implicit val decodeUser: Decoder[User] =
    Decoder.forProduct6("id", "email", "first_name", "last_name", "gender", "birth_date")(User.apply)
  implicit val encodeUser: Encoder[User] =
    Encoder.forProduct6("id", "email", "first_name", "last_name", "gender", "birth_date")(u =>
      (u.id, u.email, u.firstName, u.lastName, u.gender, u.birthDate)
    )

  implicit val decodeVisit: Decoder[Visit] =
    Decoder.forProduct5("id", "location", "user", "visited_at", "mark")(Visit.apply)
  implicit val encodeVisit: Encoder[Visit] =
    Encoder.forProduct5("id", "location", "user", "visited_at", "mark")(v =>
      (v.id, v.location, v.user, v.visitedAt, v.mark)
    )

  implicit val decodeLoc: Decoder[Location] =
    Decoder.forProduct5("id", "place", "country", "city", "distance")(Location.apply)
  implicit val encodeLoc: Encoder[Location] =
    Encoder.forProduct5("id", "place", "country", "city", "distance")(l =>
      (l.id, l.place, l.country, l.city, l.distance)
    )

  implicit val decodeUsers: Decoder[Users] =
    Decoder.forProduct1("users")(Users.apply)
  implicit val decodeVisits: Decoder[Visits] =
    Decoder.forProduct1("visits")(Visits.apply)
  implicit val decodeLocations: Decoder[Locations] =
    Decoder.forProduct1("locations")(Locations.apply)

//  implicit val userFormat = jsonFormat6(User)
//  implicit val visitFormat = jsonFormat5(Visit)
//  implicit val locationFormat = jsonFormat5(Location)
//
//  implicit val usersFormat = jsonFormat1(Users)
//  implicit val visitsFormat = jsonFormat1(Visits)
//  implicit val locationsFormat = jsonFormat1(Locations)


}
