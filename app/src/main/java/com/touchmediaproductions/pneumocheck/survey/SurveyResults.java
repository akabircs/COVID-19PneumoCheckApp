package com.touchmediaproductions.pneumocheck.survey;

import android.os.Parcel;
import android.os.Parcelable;

public class SurveyResults implements Parcelable {
    //age
    private String age;
    //biosex
    private String sex;
    //medcondition
    private String medicalConditions;
    //smoking
    private String smoker;
    //todaysymptom
    private String todaySymptom;
    //covidresults
    private String covidResults;
    //hospitalstatus
    private String hospitalised;
    //surveystate
    private String surveyState;

    public SurveyResults(){
    }

    protected SurveyResults(Parcel in) {
        age = in.readString();
        sex = in.readString();
        medicalConditions = in.readString();
        smoker = in.readString();
        todaySymptom = in.readString();
        covidResults = in.readString();
        hospitalised = in.readString();
        surveyState = in.readString();
    }

    public static final Creator<SurveyResults> CREATOR = new Creator<SurveyResults>() {
        @Override
        public SurveyResults createFromParcel(Parcel in) {
            return new SurveyResults(in);
        }

        @Override
        public SurveyResults[] newArray(int size) {
            return new SurveyResults[size];
        }
    };

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getSmoker() {
        return smoker;
    }

    public void setSmoker(String smoker) {
        this.smoker = smoker;
    }

    public String getCovidResults() {
        return covidResults;
    }

    public void setCovidResults(String covidResults) {
        this.covidResults = covidResults;
    }

    public String getHospitalised() {
        return hospitalised;
    }

    public void setHospitalised(String hospitalised) {
        this.hospitalised = hospitalised;
    }

    public String getTodaySymptom() {
        return todaySymptom;
    }

    public void setTodaySymptom(String todaysymptom) {
        this.todaySymptom = todaysymptom;
    }

    public String getSurveyState() {
        return surveyState;
    }

    public void setSurveyState(String surveyState) {
        this.surveyState = surveyState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(age);
        parcel.writeString(sex);
        parcel.writeString(medicalConditions);
        parcel.writeString(smoker);
        parcel.writeString(todaySymptom);
        parcel.writeString(covidResults);
        parcel.writeString(hospitalised);
        parcel.writeString(surveyState);
    }

    @Override
    public String toString() {
        return "SurveyResults{" +
                "age='" + age + '\'' +
                ", sex='" + sex + '\'' +
                ", medicalConditions='" + medicalConditions + '\'' +
                ", smoker='" + smoker + '\'' +
                ", todaySymptom='" + todaySymptom + '\'' +
                ", positiveCovid='" + covidResults + '\'' +
                ", hospitalised='" + hospitalised + '\'' +
                ", surveyState='" + surveyState + '\'' +
                '}';
    }


}
