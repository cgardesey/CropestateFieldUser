package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class RealmInstructorCourse extends RealmObject {

    private int id;
    @PrimaryKey
    private String instructorcourseid;
    private String instructorid;
    private String courseid;
    private String institutionid;
    private String price;
    private String currency;
    private int total_ratings;
    private float rating;
    private String created_at;
    private String updated_at;

    private String picture;
    private String name;
    private String edubackground;
    private String about;
    private String institution;
    private String coursepath;


    public RealmInstructorCourse() {

    }

    public RealmInstructorCourse(String instructorcourseid, String instructorid, String courseid, String institutionid, String price, String currency, int total_ratings, float rating, String created_at, String updated_at) {
        this.instructorcourseid = instructorcourseid;
        this.instructorid = instructorid;
        this.courseid = courseid;
        this.institutionid = institutionid;
        this.price = price;
        this.currency = currency;
        this.total_ratings = total_ratings;
        this.rating = rating;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstructorcourseid() {
        return instructorcourseid;
    }

    public void setInstructorcourseid(String instructorcourseid) {
        this.instructorcourseid = instructorcourseid;
    }

    public String getInstructorid() {
        return instructorid;
    }

    public void setInstructorid(String instructorid) {
        this.instructorid = instructorid;
    }

    public String getCourseid() {
        return courseid;
    }

    public void setCourseid(String courseid) {
        this.courseid = courseid;
    }

    public String getInstitutionid() {
        return institutionid;
    }

    public void setInstitutionid(String institutionid) {
        this.institutionid = institutionid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTotal_ratings() {
        return total_ratings;
    }

    public void setTotal_ratings(int total_ratings) {
        this.total_ratings = total_ratings;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEdubackground() {
        return edubackground;
    }

    public void setEdubackground(String edubackground) {
        this.edubackground = edubackground;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getCoursepath() {
        return coursepath;
    }

    public void setCoursepath(String coursepath) {
        this.coursepath = coursepath;
    }
}
