package com.github.nyukhalov.highloadcup.core.json

import com.github.nyukhalov.highloadcup.core.domain._
import io.circe.{Decoder, Encoder, Json}


trait DomainCodec {

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

}
