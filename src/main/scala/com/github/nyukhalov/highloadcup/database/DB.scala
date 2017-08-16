package com.github.nyukhalov.highloadcup.database

import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import com.github.nyukhalov.highloadcup.web.domain.UserVisit
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

object DB extends AppLogger {
  import Tables.users

  import Tables.locations
  import Tables.visits
  val db: Database = Database.forConfig("h2mem1")

  def init(): Future[Unit] = {

    logger.info("Users schema:")
    users.schema.createStatements.foreach(x => logger.info(x))

    logger.info("Locations schema:")
    locations.schema.createStatements.foreach(x => logger.info(x))

    logger.info("Visits schema:")
    visits.schema.createStatements.foreach(x => logger.info(x))

    val setup = (users.schema ++ locations.schema ++ visits.schema).create
    db.run(setup)
  }

  def getLocAvgRating(id: Int,
                      fromDate: Option[Long],
                      toDate: Option[Long],
                      fromBirthDate: Option[Long],
                      toBirthDate: Option[Long],
                      gender: Option[String])
                     (implicit ec: ExecutionContext): Future[Float] = {

    def getAdditionalSqlCondition(fromDate: Option[Long], toDate: Option[Long],
                                  fromBirthDate: Option[Long], toBirthDate: Option[Long],
                                  gender: Option[String]): String = {
      var res = ""

      fromDate.foreach(from => res += s"v.VISITED_AT > $from ")
      toDate.foreach(to => res += s"v.VISITED_AT < $to ")

      fromBirthDate.foreach(from => res += s"u.BIRTH_DATE >= $from ")
      toBirthDate.foreach(to => res += s"u.BIRTH_DATE < $to ")
      gender.foreach(g => res += s"u.GENDER = '$g' ")

      if (res.nonEmpty) res = "AND " + res

      res
    }

    val sqlCondition = getAdditionalSqlCondition(fromDate, toDate, fromBirthDate, toBirthDate, gender)

    val selectAction =
      sql"""
         SELECT v.MARK
         FROM VISITS v INNER JOIN USERS u ON (v.USER_ID = u.ID)
         WHERE
           v.LOC_ID = $id #$sqlCondition
       """.as[Int]

    db.run(selectAction).map(seq => {
      if (seq.isEmpty) 0f
      else seq.sum / seq.length.toFloat
    })
  }

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long],
                    country: Option[String], toDistance: Option[Int])
                   (implicit ec: ExecutionContext): Future[List[UserVisit]] = {

    var filteredVisits = visits.filter(_.userId === id)

    fromDate.foreach(fd => filteredVisits = filteredVisits.filter(_.visitedAt > fd))
    toDate.foreach(td => filteredVisits = filteredVisits.filter(_.visitedAt < td))

    var joinRes = filteredVisits join locations on(_.locationId === _.id)

    toDistance.foreach(d => joinRes = joinRes.filter(_._2.distance < d))
    country.foreach(c => joinRes = joinRes.filter(_._2.country === c))

    val q1 = for {
      (v, l) <- joinRes
    } yield (v.mark, v.visitedAt, l.place)

    val q = q1.result

    logger.info("user visits SQL:")
    q.statements.foreach(logger.info(_))

    db.run(q).map(seq => seq.toList.map {
      case (mark, visitedAt, place) => UserVisit(mark, visitedAt, place)
    })
  }

  // users
  def insertUsers(newUsers: List[User]): Future[AnyRef] = {
    val r = users ++= newUsers
    db.run(r)
  }

  def insertUser(user: User): Future[Int] = {
    val r = users += user
    db.run(r)
  }

  def updateUser(user: User): Future[Int] = {
    val q = for { u <- users if u.id === user.id } yield (u.email, u.firstName, u.lastName, u.gender, u.birthDate)
    val updateAction = q.update(user.email, user.firstName, user.lastName, user.gender, user.birthDate)
    db.run(updateAction)
  }

  def findUserById(id: Int)(implicit ec: ExecutionContext): Future[Option[User]] = {
    val r = users.filter(_.id === id).take(1).result
    db.run(r).map(_.headOption)
  }

  // locations
  def insertLocations(newLocations: List[Location]): Future[AnyRef] = {
    val r = locations ++= newLocations
    db.run(r)
  }

  def insertLocation(location: Location): Future[Int] = {
    val r = locations += location
    db.run(r)
  }

  def updateLocation(location: Location): Future[Int] = {
    val q = for { l <- locations if l.id === location.id } yield (l.place, l.country, l.city, l.distance)
    val updateAction = q.update(location.place, location.country, location.city, location.distance)
    db.run(updateAction)
  }

  def findLocationById(id: Int)(implicit ec: ExecutionContext): Future[Option[Location]] = {
    val r = locations.filter(_.id === id).take(1).result
    db.run(r).map(_.headOption)
  }

  // visits
  def insertVisits(newVisits: List[Visit]): Future[AnyRef] = {
    val r = visits ++= newVisits
    db.run(r)
  }

  def insertVisit(visit: Visit): Future[Int] = {
    val r = visits += visit
    db.run(r)
  }

  def updateVisit(visit: Visit): Future[Int] = {
    val q = for { v <- visits if v.id === visit.id } yield (v.locationId, v.userId, v.visitedAt, v.mark)
    val updateAction = q.update(visit.location, visit.user, visit.visitedAt, visit.mark)
    db.run(updateAction)
  }

  def findVisitById(id: Int)(implicit ec: ExecutionContext): Future[Option[Visit]] = {
    val r = visits.filter(_.id === id).take(1).result
    db.run(r).map(_.headOption)
  }
}
