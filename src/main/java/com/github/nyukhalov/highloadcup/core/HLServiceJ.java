package com.github.nyukhalov.highloadcup.core;

import com.github.nyukhalov.highloadcup.core.domain.*;

import java.util.List;

public interface HLServiceJ {
    // for loading from json (without validation)
    void addUsers(List<UserJ> users);
    void addLocations(List<LocationJ> locations);
    void addVisits(List<VisitJ> visits);

    // user
    boolean createUser(UserJ user);
    UserJ getUser(int id);
    boolean isUserExist(int id);
    Object updateUser(int id, UserUpdateJ userUpdate);
    Object getUserVisits(int id, Long fromDate, Long toDate, String country, Integer toDistance);

    // location
    boolean createLocation(LocationJ loc);
    LocationJ getLocation(int id);
    boolean isLocationExist(int id);
    Object updateLocation(int id, LocationUpdateJ locationUpdate);
    Object getLocAvgRating(int id, Long fromDate, Long toDate, Integer fromAge, Integer toAge, String gender);

    // visit
    boolean createVisit(VisitJ visit);
    VisitJ getVisit(int id);
    boolean isVisitExist(int id);
    Object updateVisit(int id, VisitUpdateJ visitUpdate);
}
