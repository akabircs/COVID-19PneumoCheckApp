package com.touchmediaproductions.pneumocheck.models;

import java.util.ArrayList;

public class UserProfile {
    private String accountType;
    private String displayName;
    private String surveyState;
    private String uid;
    private String age;
    private String sex;
    private ArrayList<String> associatedUsers;
    private ArrayList<String> cxr;
    private String hospitalStatus;
    private String todaySymptom;
    private String covidResults;
    private String medicalConditions;
    private String smoker;

    public UserProfile() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getSurveyState() {
        return surveyState;
    }

    public String getUid() {
        return uid;
    }

    public String getAge() {
        return age;
    }

    public String getSex() {
        return sex;
    }

    public ArrayList<String> getAssociatedUsers() {
        return associatedUsers;
    }

    public ArrayList<String> getCxr() {
        return cxr;
    }

    public String getHospitalStatus() {
        return hospitalStatus;
    }

    public String getTodaySymptom() {
        return todaySymptom;
    }

    public String getCovidResults() {
        return covidResults;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public String getSmoker() {
        return smoker;
    }

}
