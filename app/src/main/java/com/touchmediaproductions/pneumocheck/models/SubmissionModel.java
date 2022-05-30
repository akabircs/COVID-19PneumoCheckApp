package com.touchmediaproductions.pneumocheck.models;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.touchmediaproductions.pneumocheck.ml.MLHelper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model of the Submission Data type
 */
public class SubmissionModel implements Serializable {

    public enum Confirmation{
        UNCONFIRMED ("UNCONFIRMED"),
        COVID19 ("COVID-19"),
        PNEUMONIA ("Pneumonia"),
        NORMAL ("Normal"),
        COVID19FB ("COVID-19FB"),
        PNEUMONIAFB ("PneumoniaFB"),
        NORMALFB ("NormalFB");

        private final String description;

        Confirmation(String description){
            this.description = description;
        }

        public static Confirmation findByDescription(String description){
            for(Confirmation v : values()){
                if( v.getDescription().equals(description)){
                    return v;
                }
            }
            return null;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum XrayType{
        CXR, CTSCAN
    }


    private String id;
    private String firstName;
    private String lastName;
    private int age;
    private String sex;
    private Date scanCreationDate;
    private Date submissionCreationDate;
    private Confirmation confirmation;
    private MLHelper.Prediction prediction;
    private byte[] cxrPhoto;
    private String userId;
    private XrayType xrayType;
    private Timestamp learntAt;


    public SubmissionModel(String id, String userId, String firstName, String lastName, int age, String sex, Date scanCreationDate, Date submissionCreationDate, Confirmation confirmation, MLHelper.Prediction prediction, byte[] cxrayPhoto, XrayType xrayType, Timestamp learntAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.scanCreationDate = scanCreationDate;
        this.submissionCreationDate = submissionCreationDate;
        this.confirmation = confirmation;
        this.prediction = prediction;
        this.cxrPhoto = cxrayPhoto;
        this.userId = userId;
        this.xrayType = xrayType;
        this.learntAt = learntAt;
    }


    @Override
    public String toString() {
        return "SubmissionModel{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", scanCreationDate=" + scanCreationDate +
                ", submissionCreationDate=" + submissionCreationDate +
                ", confirmation=" + confirmation +
                ", prediction=" + prediction +
                ", cxrPhoto=" + Arrays.toString(cxrPhoto) +
                ", userId='" + userId + '\'' +
                ", xrayType=" + xrayType +
                ", learntAt=" + learntAt +
                '}';
    }

    public byte[] getCxrPhoto() {
        return cxrPhoto;
    }

    public void setCxrPhoto(byte[] cxrPhoto) {
        this.cxrPhoto = cxrPhoto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getAge() { return age; }

    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }

    public void setSex(String sex) { this.sex = sex; }

    public MLHelper.Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(MLHelper.Prediction prediction) {
        this.prediction = prediction;
    }

    public Date getScanCreationDate() {
        return scanCreationDate;
    }

    public void setScanCreationDate(Date scanCreationDate) {
        this.scanCreationDate = scanCreationDate;
    }

    public Date getSubmissionCreationDate() {
        return submissionCreationDate;
    }

    public void setSubmissionCreationDate(Date submissionCreationDate) {
        this.submissionCreationDate = submissionCreationDate;
    }
    public Confirmation getConfirmation() {
        return this.confirmation;
    }
    public void setConfirmation(Confirmation confirmation) { this.confirmation = confirmation; }

    public String getUserId() {
        return this.userId;
    }

    public XrayType getXrayType() {
        return this.xrayType;
    }

    public Timestamp getLearntAt() {return this.learntAt;}
}
