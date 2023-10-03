package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmLeaseUpload extends RealmObject {

    private int id;
    @PrimaryKey
    private String leaseuploadid;
    private String leasecode;
    private String title;
    private String description;
    private String url;
    private String created_at;
    private String updated_at;

    public RealmLeaseUpload() {

    }

    public RealmLeaseUpload(String leaseuploadsid, String leasecode, String title, String description, String url, String created_at, String updated_at) {
        this.leaseuploadid = leaseuploadsid;
        this.leasecode = leasecode;
        this.title = title;
        this.description = description;
        this.url = url;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLeaseuploadid() {
        return leaseuploadid;
    }

    public void setLeaseuploadid(String leaseuploadid) {
        this.leaseuploadid = leaseuploadid;
    }

    public String getLeasecode() {
        return leasecode;
    }

    public void setLeasecode(String leasecode) {
        this.leasecode = leasecode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
