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

class HLServiceImpl extends HLService with AppLogger {

  val userMap: mutable.Map[Int, User] = new ConcurrentHashMap[Int, User]() asScala
  val visitMap: mutable.Map[Int, Visit] = new ConcurrentHashMap[Int, Visit]() asScala
  val locMap: mutable.Map[Int, Location] = new ConcurrentHashMap[Int, Location]() asScala
  val locId2Visits: mutable.Map[Int, mutable.Set[Visit]] = new ConcurrentHashMap[Int, mutable.Set[Visit]]() asScala
  val userId2Visits: mutable.Map[Int, mutable.SortedSet[Visit]] = new ConcurrentHashMap[Int, mutable.SortedSet[Visit]]() asScala

  val now = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

  implicit val visitOrdering: Ordering[Visit] = (x: Visit, y: Visit) => {
    if (x.visitedAt < y.visitedAt) -1
    else if (x.visitedAt > y.visitedAt) 1
    else 0
  }

  def createConcurrentSet[T](): mutable.Set[T] = {
    import scala.collection.JavaConverters._
    java.util.Collections.newSetFromMap(
      new java.util.concurrent.ConcurrentHashMap[T, java.lang.Boolean]).asScala
  }

  def createTreeSet[T]()(implicit ordering: Ordering[T]): mutable.SortedSet[T] = {
    new mutable.TreeSet()(ordering)
  }

  private def cacheGetVisit(id: Int): Option[Visit] = visitMap.get(id)

  override def getVisit(id: Int): AnyRef = {
    cacheGetVisit(id) match {
      case Some(visit) => visit
      case None => NotExist
    }
  }

  override def createVisit(visit: Visit): AnyRef = {
    if (!VisitV.isValid(visit) || isVisitExist(visit.id) || !isUserExist(visit.user) || !isLocationExist(visit.location)) {
      Validation
    }
    else {
      visitMap += (visit.id -> visit)
      locId2Visits(visit.location).add(visit)
      userId2Visits(visit.user).add(visit)
      SuccessfulOperation
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
            visitUpdate.location.getOrElse(v.location),
            visitUpdate.user.getOrElse(v.user),
            visitUpdate.visitedAt.getOrElse(v.visitedAt),
            visitUpdate.mark.getOrElse(v.mark)
          )

          if (!isUserExist(updatedVisit.user)) Validation
          else if (!isLocationExist(updatedVisit.location)) Validation
          else {

            // location was updated
            if (v.location != updatedVisit.location) {
              locId2Visits(v.location).remove(v)
            } else {
              locId2Visits(updatedVisit.location).remove(v)
            }
            locId2Visits(updatedVisit.location).add(updatedVisit)

            // user was updated
            if (v.user != updatedVisit.user) {
              userId2Visits(v.user).remove(v)
            } else {
              userId2Visits(updatedVisit.user).remove(v)
            }
            userId2Visits(updatedVisit.user).add(updatedVisit)

            visitMap += (id -> updatedVisit)

            SuccessfulOperation
          }
      }
    }
  }

  private def cacheGetLocation(id: Int): Option[Location] = locMap.get(id)

  override def getLocation(id: Int): AnyRef = {
    cacheGetLocation(id) match {
      case Some(location) => location
      case None => NotExist
    }
  }

  override def createLocation(location: Location): AnyRef = {
    if (!LocationV.isValid(location) || isLocationExist(location.id)) {
      Validation
    } else {
      locMap += (location.id -> location)
      locId2Visits += (location.id -> createConcurrentSet())
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

      val fromBirthDate = toAge.map(ta => now.minusYears(ta).getMillis / 1000)
      val toBirthDate = fromAge.map(fa => now.minusYears(fa).getMillis / 1000)

      locId2Visits.get(locId) match {
        case None => NotExist

        case Some(visitSet) =>
          var visits = visitSet.toList
            .map(v => (v, userMap(v.user)))

          fromDate.foreach(from => visits = visits.filter(_._1.visitedAt > from))
          toDate.foreach(to => visits = visits.filter(_._1.visitedAt < to))
          fromBirthDate.foreach(from => visits = visits.filter(_._2.birthDate >= from))
          toBirthDate.foreach(to => visits = visits.filter(_._2.birthDate < to))
          gender.foreach(g => visits = visits.filter(_._2.gender == g))

          val res = if (visits.isEmpty) 0f else visits.map(_._1.mark).sum / visits.size.toFloat
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
            locationUpdate.place.getOrElse(l.place),
            locationUpdate.country.getOrElse(l.country),
            locationUpdate.city.getOrElse(l.city),
            locationUpdate.distance.getOrElse(l.distance)
          )

          locMap += (updatedLocation.id -> updatedLocation)

          SuccessfulOperation
      }
    }
  }

  override def isLocationExist(locId: Int): Boolean = {
    locMap.contains(locId)
  }

  override def addUsers(users: List[User]): Unit = {
    users.foreach(u => {
      if (createUser(u) != SuccessfulOperation) {
        throw new RuntimeException(s"Bad user $u")
      }
    })
  }

  override def addLocations(locations: List[Location]): Unit = {
    locations.foreach(l => {
      if (createLocation(l) != SuccessfulOperation) {
        throw new RuntimeException(s"Bad location $l")
      }
    })
  }

  override def addVisits(visits: List[Visit]): Unit = {
    visits.foreach(v => {
      if (createVisit(v) != SuccessfulOperation) {
        throw new RuntimeException(s"Bad visit $v")
      }
    })
  }

  override def createUser(user: User): AnyRef = {
    if (!UserV.isValid(user) || isUserExist(user.id)) {
      Validation
    } else {
      userMap += (user.id -> user)
      userId2Visits += (user.id -> createTreeSet())
      SuccessfulOperation
    }
  }

  private def cacheGetUser(id: Int): Option[User] = userMap.get(id)

  override def getUser(id: Int): AnyRef = {
    cacheGetUser(id) match {
      case Some(user) => user
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
            userUpdate.email.getOrElse(u.email),
            userUpdate.firstName.getOrElse(u.firstName),
            userUpdate.lastName.getOrElse(u.lastName),
            userUpdate.gender.getOrElse(u.gender),
            userUpdate.birthDate.getOrElse(u.birthDate)
          )

          userMap += (updatedUser.id -> updatedUser)

          SuccessfulOperation
      }
    }
  }

  override def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long],
                             country: Option[String], toDistance: Option[Int]): AnyRef = {
    if (!isUserExist(id)) {
      NotExist
    } else {
      var visits = userId2Visits(id).toList
        .map(v => (v, locMap(v.location)))

      fromDate.foreach(from => visits = visits.filter(_._1.visitedAt > from))
      toDate.foreach(to => visits = visits.filter(_._1.visitedAt < to))
      country.foreach(c => visits = visits.filter(_._2.country == c))
      toDistance.foreach(d => visits = visits.filter(_._2.distance < d))

      val res = visits
        .map(v => UserVisit(v._1.mark, v._1.visitedAt, v._2.place))

      UserVisits(res)
    }
  }
}
