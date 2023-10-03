package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmAttendance extends RealmObject {

    private int id;
    @PrimaryKey
    private String attendanceid;
    private String audioid;
    private String duration;
    private String studentid;
    private String created_at;
    private String updated_at;

    private String coursepath;
    private String instructorname;
    private String url;
    private String audiotitle;

    public RealmAttendance() {

    }

    public RealmAttendance(String attendanceid, String audioid, String duration, String studentid, String created_at, String updated_at) {
        this.attendanceid = attendanceid;
        this.audioid = audioid;
        this.duration = duration;
        this.studentid = studentid;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAttendanceid() {
        return attendanceid;
    }

    public void setAttendanceid(String attendanceid) {
        this.attendanceid = attendanceid;
    }

    public String getAudioid() {
        return audioid;
    }

    public void setAudioid(String audioid) {
        this.audioid = audioid;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
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

    public String getInstructorname() {
        return instructorname;
    }

    public void setInstructorname(String instructorname) {
        this.instructorname = instructorname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAudiotitle() {
        return audiotitle;
    }

    public void setAudiotitle(String audiotitle) {
        this.audiotitle = audiotitle;
    }
}
