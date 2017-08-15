package com.github.nyukhalov.highloadcup.database

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

object DB {
  import Tables.users
  import Tables.locations
  import Tables.visits

  val db: Database = Database.forConfig("h2mem1")

  def init(): Future[Unit] = {
    val setup = (users.schema ++ locations.schema ++ visits.schema).create
    db.run(setup)
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
