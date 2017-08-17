package com.github.nyukhalov.highloadcup.web.json

import com.github.nyukhalov.highloadcup.core.json.DomainCodec
import com.github.nyukhalov.highloadcup.web.domain._
import io.circe.Decoder.{withReattempt}
import io.circe._

trait JsonSupport extends DomainCodec {

  implicit final def decodeOption[A](implicit d: Decoder[A]): Decoder[Option[A]] = withReattempt {
    case c: HCursor =>
      if (c.value.isNull) {
        Left(DecodingFailure("null", c.history))
      } else {
        d(c) match {
          case Right(a) => Right(Some(a))
          case Left(df) if df.history.isEmpty => Right(None)
          case Left(df) => Left(df)
        }
      }
    case c: FailedCursor =>
      if (!c.incorrectFocus) Right(None) else Left(DecodingFailure("[A]Option[A]", c.history))
  }

  implicit val encodeLocAvg: Encoder[LocAvgRating] =
    Encoder.forProduct1("avg")(x => x.avg)

  implicit val encodeUserVisit: Encoder[UserVisit] =
    Encoder.forProduct3("mark", "visited_at", "place")(x => (x.mark, x.visitedAt, x.place))

  implicit val encodeUserVisits: Encoder[UserVisits] =
    Encoder.forProduct1("visits")(x => x.visits)

  implicit val decodeLocUpdate: Decoder[LocationUpdate] =
    Decoder.forProduct4("place", "country", "city", "distance")(LocationUpdate.apply)
  implicit val decodeUserUpdate: Decoder[UserUpdate] =
    Decoder.forProduct5("email", "first_name", "last_name", "gender", "birth_date")(UserUpdate.apply)
  implicit val decodeVisitUpdate: Decoder[VisitUpdate] =
    Decoder.forProduct4("location", "user", "visited_at", "mark")(VisitUpdate.apply)
}
