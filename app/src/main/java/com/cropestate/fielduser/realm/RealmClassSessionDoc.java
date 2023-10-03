package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmClassSessionDoc extends RealmObject {

    @PrimaryKey
    private String url;
    private String instructorcourseid;

    public RealmClassSessionDoc() {

    }

    public RealmClassSessionDoc(String url, String instructorcourseid) {
        this.url = url;
        this.instructorcourseid = instructorcourseid;
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
}
