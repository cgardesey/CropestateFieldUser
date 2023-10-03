package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmLease extends RealmObject {

    @PrimaryKey
    private String CODE;
    private String EMAIL_ID;
    private String CROP;
    private String LOCATION;
    private String INVESTMENT;
    private String SIZE;
    private String LAND_TYPE;
    private String STATUS;
    private String IMAGE;
    private String ID;
    private String COORDINATES;
    private String DATE_CREATED;

    private String NAME;

    public RealmLease() {

    }

    public RealmLease(String CODE, String EMAIL_ID, String CROP, String LOCATION, String INVESTMENT, String SIZE, String LAND_TYPE, String STATUS, String IMAGE, String ID, String COORDINATES, String DATE_CREATED) {
        this.CODE = CODE;
        this.EMAIL_ID = EMAIL_ID;
        this.CROP = CROP;
        this.LOCATION = LOCATION;
        this.INVESTMENT = INVESTMENT;
        this.SIZE = SIZE;
        this.LAND_TYPE = LAND_TYPE;
        this.STATUS = STATUS;
        this.IMAGE = IMAGE;
        this.ID = ID;
        this.COORDINATES = COORDINATES;
        this.DATE_CREATED = DATE_CREATED;
    }

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public String getEMAIL_ID() {
        return EMAIL_ID;
    }

    public void setEMAIL_ID(String EMAIL_ID) {
        this.EMAIL_ID = EMAIL_ID;
    }

    public String getCROP() {
        return CROP;
    }

    public void setCROP(String CROP) {
        this.CROP = CROP;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }

    public String getINVESTMENT() {
        return INVESTMENT;
    }

    public void setINVESTMENT(String INVESTMENT) {
        this.INVESTMENT = INVESTMENT;
    }

    public String getSIZE() {
        return SIZE;
    }

    public void setSIZE(String SIZE) {
        this.SIZE = SIZE;
    }

    public String getLAND_TYPE() {
        return LAND_TYPE;
    }

    public void setLAND_TYPE(String LAND_TYPE) {
        this.LAND_TYPE = LAND_TYPE;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getIMAGE() {
        return IMAGE;
    }

    public void setIMAGE(String IMAGE) {
        this.IMAGE = IMAGE;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCOORDINATES() {
        return COORDINATES;
    }

    public void setCOORDINATES(String COORDINATES) {
        this.COORDINATES = COORDINATES;
    }

    public String getDATE_CREATED() {
        return DATE_CREATED;
    }

    public void setDATE_CREATED(String DATE_CREATED) {
        this.DATE_CREATED = DATE_CREATED;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }
}
