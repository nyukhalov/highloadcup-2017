package com.github.nyukhalov.highloadcup.database

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import slick.jdbc.H2Profile.api._

object Tables {
  class Users(tag: Tag) extends Table[User](tag, "USERS"){
    def id = column[Int]("ID", O.PrimaryKey)
    def email = column[String]("EMAIL")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def gender = column[String]("GENDER")
    def birthDate = column[Long]("BIRTH_DATE")

    override def * = (id, email, firstName, lastName, gender, birthDate) <> (User.tupled, User.unapply)

    def genderIdx = index("idx_gender", gender)
    def birthDateIdx = index("idx_bd", birthDate)
  }
  val users: TableQuery[Users] = TableQuery[Users]

  class Locations(tag: Tag) extends Table[Location](tag, "LOCATIONS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def place = column[String]("PLACE")
    def country = column[String]("COUNTRY")
    def city = column[String]("CITY")
    def distance = column[Int]("DISTANCE")

    override def * = (id, place, country, city, distance) <> (Location.tupled, Location.unapply)

    def distanceIdx = index("idx_distance", distance)
    def countryIdx = index("idx_country", country)
  }
  val locations: TableQuery[Locations] = TableQuery[Locations]

  class Visits(tag: Tag) extends Table[Visit](tag, "VISITS") {
    def id = column[Int]("ID", O.PrimaryKey)
    def locationId = column[Int]("LOC_ID")
    def userId = column[Int]("USER_ID")
    def visitedAt = column[Long]("VISITED_AT")
    def mark = column[Int]("MARK")

    override def * = (id, locationId, userId, visitedAt, mark) <> (Visit.tupled, Visit.unapply)

    def location = foreignKey("LOC_FK", locationId, locations)(_.id)
    def user = foreignKey("USER_FK", userId, users)(_.id)

    def visitedAtIdx = index("idx_visited_at", visitedAt)
  }
  val visits: TableQuery[Visits] = TableQuery[Visits]
}
