package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmEnrolment extends RealmObject {
    private int id;
    @PrimaryKey
    private String enrolmentid;
    private String studentid;
    private String instructorcourseid;
    private boolean approved;
    private int enrolled;
    private int percentagecompleted;
    private String created_at;
    private String updated_at;

    private String coursepath;
    private String dow;
    private String downum;
    private String starttime;
    private String endtime;
    private String time;
    private String classsessionid;
    private String instructorname;
    private String profilepicurl;
    private float rating;
    private int totalrating;
    private boolean activelysubscribed;
    private boolean hassubsribedbefore;
    private String price;
    private String subsriptionexpirydate;
    private String currency;
    private boolean live;
    private boolean upcoming;
    private boolean ratedbyme;


    public RealmEnrolment() {

    }

    public RealmEnrolment(String enrolmentid, String studentid, String instructorcourseid, boolean approved, int enrolled, int percentagecompleted, String created_at, String updated_at) {
        this.enrolmentid = enrolmentid;
        this.studentid = studentid;
        this.instructorcourseid = instructorcourseid;
        this.approved = approved;
        this.enrolled = enrolled;
        this.percentagecompleted = percentagecompleted;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnrolmentid() {
        return enrolmentid;
    }

    public void setEnrolmentid(String enrolmentid) {
        this.enrolmentid = enrolmentid;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getInstructorcourseid() {
        return instructorcourseid;
    }

    public void setInstructorcourseid(String instructorcourseid) {
        this.instructorcourseid = instructorcourseid;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isHassubsribedbefore() {
        return hassubsribedbefore;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled = enrolled;
    }

    public int getPercentagecompleted() {
        return percentagecompleted;
    }

    public void setPercentagecompleted(int percentagecompleted) {
        this.percentagecompleted = percentagecompleted;
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

    public String getCoursepath() {
        return coursepath;
    }

    public void setCoursepath(String coursepath) {
        this.coursepath = coursepath;
    }

    public String getDow() {
        return dow;
    }

    public void setDow(String dow) {
        this.dow = dow;
    }

    public String getDownum() {
        return downum;
    }

    public void setDownum(String downum) {
        this.downum = downum;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClasssessionid() {
        return classsessionid;
    }

    public void setClasssessionid(String classsessionid) {
        this.classsessionid = classsessionid;
    }

    public String getInstructorname() {
        return instructorname;
    }

    public void setInstructorname(String instructorname) {
        this.instructorname = instructorname;
    }

    public String getProfilepicurl() {
        return profilepicurl;
    }

    public void setProfilepicurl(String profilepicurl) {
        this.profilepicurl = profilepicurl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getTotalrating() {
        return totalrating;
    }

    public void setTotalrating(int totalrating) {
        this.totalrating = totalrating;
    }

    public boolean isActivelysubscribed() {
        return activelysubscribed;
    }

    public void setActivelysubscribed(boolean activelysubscribed) {
        this.activelysubscribed = activelysubscribed;
    }

    public boolean hassubsribedbefore() {
        return hassubsribedbefore;
    }

    public void setHassubsribedbefore(boolean hassubsribedbefore) {
        this.hassubsribedbefore = hassubsribedbefore;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSubsriptionexpirydate() {
        return subsriptionexpirydate;
    }

    public void setSubsriptionexpirydate(String subsriptionexpirydate) {
        this.subsriptionexpirydate = subsriptionexpirydate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isUpcoming() {
        return upcoming;
    }

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    public boolean isRatedbyme() {
        return ratedbyme;
    }

    public void setRatedbyme(boolean ratedbyme) {
        this.ratedbyme = ratedbyme;
    }
}
