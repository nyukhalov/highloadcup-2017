package com.github.nyukhalov.highloadcup.core

import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.web.domain._
import org.joda.time.{DateTime, DateTimeZone}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._
import scala.collection.mutable

trait HLService {

  // UTIL

  def addUsers(users: List[User]): Unit

  def addLocations(locations: List[Location]): Unit

  def addVisits(visits: List[Visit]): Unit

  // USER

  def createUser(user: User): AnyRef

  def getUser(id: Int): AnyRef

  def isUserExist(userId: Int): Boolean

  def updateUser(id: Int, userUpdate: UserUpdate): AnyRef

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long],
                    country: Option[String], toDistance: Option[Int]): AnyRef

  // VISIT

  def getVisit(id: Int): AnyRef

  def createVisit(visit: Visit): AnyRef

  def isVisitExist(visitId: Int): Boolean

  def updateVisit(id: Int, visitUpdate: VisitUpdate): AnyRef

  // LOCATION

  def getLocation(id: Int): AnyRef

  def createLocation(location: Location): AnyRef

  def isLocationExist(locId: Int): Boolean

  def updateLocation(id: Int, locationUpdate: LocationUpdate): AnyRef

  def getAverageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): AnyRef
}

class HLServiceImpl extends HLService {

  val userMap: mutable.Map[Int, User2] = new ConcurrentHashMap[Int, User2]() asScala
  val visitMap: mutable.Map[Int, Visit2] = new ConcurrentHashMap[Int, Visit2]() asScala
  val locMap: mutable.Map[Int, Location2] = new ConcurrentHashMap[Int, Location2]() asScala


  private def cacheGetVisit(id: Int): Option[Visit2] = visitMap.get(id)

  override def getVisit(id: Int): AnyRef = {
    cacheGetVisit(id) match {
      case Some(visit) => visit.visit
      case None => NotExist
    }
  }

  override def createVisit(visit: Visit): AnyRef = {
    if (!VisitV.isValid(visit) || isVisitExist(visit.id)) Validation
    else {
      val user = cacheGetUser(visit.user)
      val loc = cacheGetLocation(visit.location)

      (user, loc) match {
        case (Some(u), Some(l)) =>
          val visit2 = Visit2(visit, l.location, u.user)
          u.visits.add(visit2)
          l.visits.add(visit2)
          visitMap += (visit.id -> visit2)
          SuccessfulOperation

        case _ => Validation
      }
    }
  }

  override def isVisitExist(visitId: Int): Boolean = {
    visitMap.contains(visitId)
  }

  private def isValidVisitUpdate(vu: VisitUpdate) = {
    if (vu.mark.isDefined && !VisitV.isValidMark(vu.mark.get)) false
    else if (vu.visitedAt.isDefined && !VisitV.isValidVisitedAt(vu.visitedAt.get)) false
    else true
  }

  override def updateVisit(id: Int, visitUpdate: VisitUpdate): AnyRef = {
    if (!isValidVisitUpdate(visitUpdate)) {
      Validation
    } else {
      cacheGetVisit(id) match {
        case None => NotExist

        case Some(v) =>
          val updatedVisit = Visit(
            id,
            visitUpdate.location.getOrElse(v.visit.location),
            visitUpdate.user.getOrElse(v.visit.user),
            visitUpdate.visitedAt.getOrElse(v.visit.visitedAt),
            visitUpdate.mark.getOrElse(v.visit.mark)
          )
          val loc = cacheGetLocation(updatedVisit.location)
          val user = cacheGetUser(updatedVisit.user)

          (user, loc) match {
            case (Some(u), Some(l)) =>
              // if loc was updated
              if (v.location.id != updatedVisit.location) {
                val oldLoc = cacheGetLocation(v.location.id).get
                val v2 = oldLoc.visits.filter(_.location.id == v.location.id).head
                oldLoc.visits.remove(v2)
              }

              // if user was updated
              if (v.user.id != updatedVisit.user) {
                val oldUser = cacheGetUser(v.user.id).get
                val v2 = oldUser.visits.filter(_.user.id == v.user.id).head
                oldUser.visits.remove(v2)
              }

              val visit2 = Visit2(updatedVisit, l.location, u.user)
              l.visits.add(visit2)
              u.visits.add(visit2)

              visitMap += (updatedVisit.id -> visit2)
              SuccessfulOperation

            case _ => Validation
          }
      }
    }
  }

  private def cacheGetLocation(id: Int): Option[Location2] = locMap.get(id)

  override def getLocation(id: Int): AnyRef = {
    cacheGetLocation(id) match {
      case Some(location) => location.location
      case None => NotExist
    }
  }

  override def createLocation(location: Location): AnyRef = {
    if (!LocationV.isValid(location) || isLocationExist(location.id)) {
      Validation
    } else {
      locMap += (location.id -> Location2(location, mutable.Set()))
      SuccessfulOperation
    }
  }

  private def isValidLocationUpdate(lu: LocationUpdate) = {
    if (lu.place.isDefined && !LocationV.isValidPlace(lu.place.get)) false
    else if (lu.country.isDefined && !LocationV.isValidCountry(lu.country.get)) false
    else if (lu.city.isDefined && !LocationV.isValidCity(lu.city.get)) false
    else if (lu.distance.isDefined && !LocationV.isValidDistance(lu.distance.get)) false
    else true
  }

  private def isValidParams(fromDate: Option[Long], toDate: Option[Long],
                            fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Boolean = {
    fromAge.getOrElse(1) > 0 &&
      toAge.getOrElse(1) > 0 &&
      UserV.isValidGender(gender.getOrElse("m"))
  }

  override def getAverageRating(locId: Int, fromDate: Option[Long], toDate: Option[Long],
                                fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): AnyRef = {
    if (!isValidParams(fromDate, toDate, fromAge, toAge, gender)) {
      Validation
    } else {

      val now = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

      val fromBirthDate = toAge.map(ta => now.minusYears(ta).getMillis / 1000)
      val toBirthDate = fromAge.map(fa => now.minusYears(fa).getMillis / 1000)

      cacheGetLocation(locId) match {
        case None => NotExist

        case Some(l) =>
          var visits = l.visits
          fromDate.foreach(from => visits = visits.filter(_.visit.visitedAt > from))
          toDate.foreach(to => visits = visits.filter(_.visit.visitedAt < to))
          fromBirthDate.foreach(from => visits = visits.filter(_.user.birthDate >= from))
          toBirthDate.foreach(to => visits = visits.filter(_.user.birthDate < to))
          gender.foreach(g => visits = visits.filter(_.user.gender == g))

          val res = if (visits.isEmpty) 0f else visits.map(_.visit.mark).sum / visits.size.toFloat
          LocAvgRating(BigDecimal(res).setScale(5, BigDecimal.RoundingMode.HALF_UP).toFloat)
      }
    }
  }

  override def updateLocation(id: Int, locationUpdate: LocationUpdate): AnyRef = {
    if (!isValidLocationUpdate(locationUpdate)) {
      Validation
    } else {

      cacheGetLocation(id) match {
        case None => NotExist

        case Some(l) =>
          val updatedLocation = Location(
            id,
            locationUpdate.place.getOrElse(l.location.place),
            locationUpdate.country.getOrElse(l.location.country),
            locationUpdate.city.getOrElse(l.location.city),
            locationUpdate.distance.getOrElse(l.location.distance)
          )
          // TODO: update visits
          locMap += (updatedLocation.id -> Location2(updatedLocation, l.visits))
          SuccessfulOperation
      }
    }
  }

  override def isLocationExist(locId: Int): Boolean = {
    locMap.contains(locId)
  }

  override def addUsers(users: List[User]): Unit = {
    users.foreach(u => createUser(u))
  }

  override def addLocations(locations: List[Location]): Unit = {
    locations.foreach(l => createLocation(l))
  }

  override def addVisits(visits: List[Visit]): Unit = {
    visits.foreach(v => createVisit(v))
  }

  override def createUser(user: User): AnyRef = {
    if (!UserV.isValid(user) || isUserExist(user.id)) {
      Validation
    } else {
      userMap += (user.id -> User2(user, mutable.Set()))
      SuccessfulOperation
    }
  }

  private def cacheGetUser(id: Int): Option[User2] = userMap.get(id)

  override def getUser(id: Int): AnyRef = {
    cacheGetUser(id) match {
      case Some(user) => user.user
      case None => NotExist
    }
  }

  override def isUserExist(userId: Int): Boolean = {
    userMap.contains(userId)
  }

  private def isValidUserUpdate(uu: UserUpdate) = {
    if (uu.email.isDefined && !UserV.isValidEmail(uu.email.get)) false
    else if (uu.firstName.isDefined && !UserV.isValidName(uu.firstName.get)) false
    else if (uu.lastName.isDefined && !UserV.isValidName(uu.lastName.get)) false
    else if (uu.gender.isDefined && !UserV.isValidGender(uu.gender.get)) false
    else if (uu.birthDate.isDefined && !UserV.isValidBirthDate(uu.birthDate.get)) false
    else true
  }

  override def updateUser(id: Int, userUpdate: UserUpdate): AnyRef = {
    if (!isValidUserUpdate(userUpdate)) {
      Validation
    } else {
      cacheGetUser(id) match {
        case None => NotExist

        case Some(u) =>
          val updatedUser = User(
            id,
            userUpdate.email.getOrElse(u.user.email),
            userUpdate.firstName.getOrElse(u.user.firstName),
            userUpdate.lastName.getOrElse(u.user.lastName),
            userUpdate.gender.getOrElse(u.user.gender),
            userUpdate.birthDate.getOrElse(u.user.birthDate)
          )
          // TODO: update visits
          userMap += (updatedUser.id -> User2(updatedUser, u.visits))
          SuccessfulOperation
      }
    }
  }

  override def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long],
                             country: Option[String], toDistance: Option[Int]): AnyRef = {
    cacheGetUser(id) match {
      case None => NotExist
      case Some(u) =>
        var visits = u.visits
        fromDate.foreach(from => visits = visits.filter(_.visit.visitedAt > from))
        toDate.foreach(to => visits = visits.filter(_.visit.visitedAt < to))
        country.foreach(c => visits = visits.filter(_.location.country == c))
        toDistance.foreach(d => visits = visits.filter(_.location.distance < d))

        val res = visits
          .map(v => UserVisit(v.visit.mark, v.visit.visitedAt, v.location.place))
          .toList
          .sortBy(_.visitedAt)
        UserVisits(res)
    }
  }
}
