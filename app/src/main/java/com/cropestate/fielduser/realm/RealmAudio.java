package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmAudio extends RealmObject {

    private int id;
    @PrimaryKey
    private String audioid;
    private String sessionid;
    private String title;
    private String url;
    private String instructorcourseid;
    private String created_at;
    private String updated_at;

    private boolean attended;
    private String coursepath;

    public RealmAudio() {

    }

    public RealmAudio(String audioid, String sessionid, String title, String url, String instructorcourseid, String created_at, String updated_at) {
        this.audioid = audioid;
        this.sessionid = sessionid;
        this.title = title;
        this.url = url;
        this.instructorcourseid = instructorcourseid;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAudioid() {
        return audioid;
    }

    public void setAudioid(String audioid) {
        this.audioid = audioid;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInstructorcourseid() {
        return instructorcourseid;
    }

    public void setInstructorcourseid(String instructorcourseid) {
        this.instructorcourseid = instructorcourseid;
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

    public boolean attended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public String getCoursepath() {
        return coursepath;
    }

    public void setCoursepath(String coursepath) {
        this.coursepath = coursepath;
    }
}
