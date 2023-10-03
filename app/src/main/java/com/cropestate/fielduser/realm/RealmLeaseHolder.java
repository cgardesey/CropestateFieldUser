package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmLeaseHolder extends RealmObject {

    @PrimaryKey
    private String EMAIL_ID;
    private String NAME;
    private String CONTACT;

    public RealmLeaseHolder() {

    }

    public RealmLeaseHolder(String EMAIL_ID, String NAME, String CONTACT) {
        this.EMAIL_ID = EMAIL_ID;
        this.NAME = NAME;
        this.CONTACT = CONTACT;
    }

    public String getEMAIL_ID() {
        return EMAIL_ID;
    }

    public void setEMAIL_ID(String EMAIL_ID) {
        this.EMAIL_ID = EMAIL_ID;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getCONTACT() {
        return CONTACT;
    }

    public void setCONTACT(String CONTACT) {
        this.CONTACT = CONTACT;
    }
}
