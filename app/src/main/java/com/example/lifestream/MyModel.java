package com.example.lifestream;

public class MyModel {

    String bloodgroup, email, name, phone, profilepic, search, usertype, lat, lng;
    Double distance;

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public MyModel(String bloodgroup, String email, String name, String phone, String profilepic, String search, String usertype, String lat, String lng, Double distance) {
        this.bloodgroup = bloodgroup;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.profilepic = profilepic;
        this.search = search;
        this.usertype = usertype;
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }

    MyModel()
    {

    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }


    public MyModel(String bloodgroup, String email, String name, String phone, String profilepic, String search, String usertype, String lat, String lng) {
        this.bloodgroup = bloodgroup;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.profilepic = profilepic;
        this.search = search;
        this.usertype = usertype;
        this.lat = lat;
        this.lng = lng;
    }

    public MyModel(String bloodgroup, String email, String name, String phone, String profilepic, String search, String usertype) {
        this.bloodgroup = bloodgroup;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.profilepic = profilepic;
        this.search = search;
        this.usertype = usertype;
    }

    public String getBloodgroup() {
        return bloodgroup;
    }

    public void setBloodgroup(String bloodgroup) {
        this.bloodgroup = bloodgroup;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}
