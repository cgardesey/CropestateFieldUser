package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmBanner extends RealmObject {

    @PrimaryKey
    private String url;
    private String criteria,expirydate;

    public RealmBanner() {
    }

    public RealmBanner(String url, String criteria, String expirydate) {
        this.url = url;
        this.criteria = criteria;
        this.expirydate = expirydate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(String expirydate) {
        this.expirydate = expirydate;
    }
}
