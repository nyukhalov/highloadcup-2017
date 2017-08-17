package com.github.nyukhalov.highloadcup.core

import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.Future

trait HLService {

  def addUsers(users: List[User]): Future[AnyRef]
  def addLocations(locations: List[Location]): Future[AnyRef]
  def addVisits(visits: List[Visit]): Future[AnyRef]

  // USER

  def createUser(user: User): Future[AnyRef]

  def getUser(id: Int): AnyRef

  def isUserExist(userId: Int): Boolean

  def updateUser(id: Int, userUpdate: UserUpdate): Future[AnyRef]

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long],
                    country: Option[String], toDistance: Option[Int]): Future[AnyRef]

  // VISIT

  def getVisit(id: Int): AnyRef

  def createVisit(visit: Visit): Future[AnyRef]

  def isVisitExist(visitId: Int): Boolean

  def updateVisit(id: Int, visitUpdate: VisitUpdate): Future[AnyRef]

  // LOCATION

  def getLocation(id: Int): AnyRef

  def createLocation(location: Location): Future[AnyRef]

  def isLocationExist(locId: Int): Boolean

  def updateLocation(id: Int, locationUpdate: LocationUpdate): Future[AnyRef]

  def getAverageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Future[AnyRef]
}

class HLServiceImpl extends HLService {

  import scala.concurrent.ExecutionContext.Implicits.global

  var userMap: Map[Int, User] = Map[Int, User]()
  var visitMap: Map[Int, Visit] = Map[Int, Visit]()
  var locMap: Map[Int, Location] = Map[Int, Location]()


  private def cacheGetVisit(id: Int): Option[Visit] = visitMap.get(id)

  override def getVisit(id: Int): AnyRef = {
    cacheGetVisit(id) match {
      case Some(visit) => visit
      case None => NotExist
    }
  }

  override def createVisit(visit: Visit): Future[AnyRef] = {
    if (!VisitV.isValid(visit)) {
      Future.successful(Validation)
    } else {
      if (!isVisitExist(visit.id) && isUserExist(visit.user) && isLocationExist(visit.location)) {
        visitMap.synchronized {
          visitMap = visitMap + (visit.id -> visit)
        }
        DB.insertVisit(visit).map(_ => SuccessfulOperation)
      }
      else {
        Future.successful(Validation)
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

  override def updateVisit(id: Int, visitUpdate: VisitUpdate): Future[AnyRef] = {
    if (!isValidVisitUpdate(visitUpdate)) {
      Future.successful(Validation)
    } else {

      cacheGetVisit(id) match {
        case None => Future.successful(NotExist)

        case Some(v) =>
          val updatedVisit = Visit(
            id,
            visitUpdate.location.getOrElse(v.location),
            visitUpdate.user.getOrElse(v.user),
            visitUpdate.visitedAt.getOrElse(v.visitedAt),
            visitUpdate.mark.getOrElse(v.mark)
          )

          visitMap.synchronized{
            visitMap = visitMap + (updatedVisit.id -> updatedVisit)
          }
          DB.updateVisit(updatedVisit).map(_ => SuccessfulOperation)
      }
    }
  }


  /// LOCATION
  private def cacheGetLocation(id: Int): Option[Location] = locMap.get(id)

  override def getLocation(id: Int): AnyRef = {
    cacheGetLocation(id) match {
      case Some(location) => location
      case None => NotExist
    }
  }

  override def createLocation(location: Location): Future[AnyRef] = {
    if (!LocationV.isValid(location) || isLocationExist(location.id)) {
      Future.successful(Validation)
    } else {
      locMap.synchronized {
        locMap = locMap + (location.id -> location)
      }
      DB.insertLocation(location).map(_ => SuccessfulOperation)
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

  override def getAverageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Future[AnyRef] = {
    if (!isValidParams(fromDate, toDate, fromAge, toAge, gender)) {
      Future.successful(Validation)
    } else {

      val now = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

      val fromBirthDate = toAge.map(ta => now.minusYears(ta).getMillis / 1000)
      val toBirthDate = fromAge.map(fa => now.minusYears(fa).getMillis / 1000)

      isLocationExist(id) match {
        case true => DB.getLocAvgRating(id, fromDate, toDate, fromBirthDate, toBirthDate, gender).map(res => LocAvgRating(res))

        case false => Future.successful(NotExist)
      }
    }
  }

  override def updateLocation(id: Int, locationUpdate: LocationUpdate): Future[AnyRef] = {
    if (!isValidLocationUpdate(locationUpdate)) {
      Future.successful(Validation)
    } else {

      cacheGetLocation(id) match {
        case None => Future.successful(NotExist)

        case Some(l) =>
          val updatedLocation = Location(
            id,
            locationUpdate.place.getOrElse(l.place),
            locationUpdate.country.getOrElse(l.country),
            locationUpdate.city.getOrElse(l.city),
            locationUpdate.distance.getOrElse(l.distance)
          )

          locMap.synchronized {
            locMap = locMap + (updatedLocation.id -> updatedLocation)
          }
          DB.updateLocation(updatedLocation).map(_ => SuccessfulOperation)
      }
    }
  }

  override def isLocationExist(locId: Int): Boolean = {
    locMap.contains(locId)
  }

  override def addUsers(users: List[User]): Future[AnyRef] = {
    userMap.synchronized {
      users.foreach(u => userMap = userMap + (u.id -> u))
    }
    DB.insertUsers(users)
  }

  override def addLocations(locations: List[Location]) = {
    locMap.synchronized {
      locations.foreach(l => locMap = locMap + (l.id -> l))
    }
    DB.insertLocations(locations)
  }

  override def addVisits(visits: List[Visit]) = {
    visitMap.synchronized {
      visits.foreach(v => visitMap = visitMap + (v.id -> v))
    }
    DB.insertVisits(visits)
  }

  override def createUser(user: User): Future[AnyRef] = {
    if (!UserV.isValid(user)) {
      Future.successful(Validation)
    } else {
      isUserExist(user.id) match {
        case true => Future.successful(Validation)
        case false =>
          userMap.synchronized {
            userMap = userMap + (user.id -> user)
          }
          DB.insertUser(user).map(_ => SuccessfulOperation)
      }
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
    else if (uu.birthDate.isDefined && !UserV.isValidBirthDate(uu.birthDate.get)) false
    else true
  }

  override def updateUser(id: Int, userUpdate: UserUpdate): Future[AnyRef] = {
    if (!isValidUserUpdate(userUpdate)) {
      Future.successful(Validation)
    } else {
      cacheGetUser(id) match {
        case None => Future.successful(NotExist)

        case Some(u) =>
          val updatedUser = User(
            id,
            userUpdate.email.getOrElse(u.email),
            userUpdate.firstName.getOrElse(u.firstName),
            userUpdate.lastName.getOrElse(u.lastName),
            userUpdate.gender.getOrElse(u.gender),
            userUpdate.birthDate.getOrElse(u.birthDate)
          )

          userMap.synchronized{
            userMap = userMap + (updatedUser.id -> updatedUser)
          }
          DB.updateUser(updatedUser).map(_ => SuccessfulOperation)
      }
    }
  }

  override def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]): Future[AnyRef] = {
    isUserExist(id) match {
      case false => Future.successful(NotExist)
      case true => DB.getUserVisits(id, fromDate, toDate, country, toDistance).map(v => UserVisits(v.sortBy(_.visitedAt)))
    }
  }
}
