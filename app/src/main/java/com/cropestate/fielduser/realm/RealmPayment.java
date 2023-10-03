package com.cropestate.fielduser.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmPayment extends RealmObject {

    private int id;
    @PrimaryKey
    private String paymentid;
    private String msisdn;
    private String countrycode;
    private String network;
    private String currency;
    private String amount;
    private String description;
    private String paymentref;
    private String externalreferenceno;
    private String message;
    private String status;
    private String expirydate;
    private String payerid;
    private String enrolmentid;
    private boolean expired;
    private String created_at;
    private String updated_at;


    private String coursepath;

    public RealmPayment() {

    }

    public RealmPayment(String paymentid, String msisdn, String countrycode, String network, String currency, String amount, String description, String paymentref, String externalreferenceno, String message, String status, String expirydate, String payerid, String enrolmentid, boolean expired, String created_at, String updated_at) {
        this.paymentid = paymentid;
        this.msisdn = msisdn;
        this.countrycode = countrycode;
        this.network = network;
        this.currency = currency;
        this.amount = amount;
        this.description = description;
        this.paymentref = paymentref;
        this.externalreferenceno = externalreferenceno;
        this.message = message;
        this.status = status;
        this.expirydate = expirydate;
        this.payerid = payerid;
        this.enrolmentid = enrolmentid;
        this.expired = expired;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(String paymentid) {
        this.paymentid = paymentid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentref() {
        return paymentref;
    }

    public void setPaymentref(String paymentref) {
        this.paymentref = paymentref;
    }

    public String getExternalreferenceno() {
        return externalreferenceno;
    }

    public void setExternalreferenceno(String externalreferenceno) {
        this.externalreferenceno = externalreferenceno;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(String expirydate) {
        this.expirydate = expirydate;
    }

    public String getPayerid() {
        return payerid;
    }

    public void setPayerid(String payerid) {
        this.payerid = payerid;
    }

    public String getEnrolmentid() {
        return enrolmentid;
    }

    public void setEnrolmentid(String enrolmentid) {
        this.enrolmentid = enrolmentid;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
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
}
