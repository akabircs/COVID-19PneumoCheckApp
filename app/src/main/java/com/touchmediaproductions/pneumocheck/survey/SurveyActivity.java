package com.touchmediaproductions.pneumocheck.survey;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.quickbirdstudios.surveykit.AnswerFormat;
import com.quickbirdstudios.surveykit.FinishReason;
import com.quickbirdstudios.surveykit.OrderedTask;
import com.quickbirdstudios.surveykit.StepIdentifier;
import com.quickbirdstudios.surveykit.SurveyTheme;
import com.quickbirdstudios.surveykit.TaskIdentifier;
import com.quickbirdstudios.surveykit.TextChoice;
import com.quickbirdstudios.surveykit.result.StepResult;
import com.quickbirdstudios.surveykit.result.TaskResult;
import com.quickbirdstudios.surveykit.steps.CompletionStep;
import com.quickbirdstudios.surveykit.steps.InstructionStep;
import com.quickbirdstudios.surveykit.steps.QuestionStep;
import com.quickbirdstudios.surveykit.steps.Step;
import com.quickbirdstudios.surveykit.survey.SurveyView;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class SurveyActivity extends AppCompatActivity {
    public static final int RUN_SURVEY = 211;
    public static final int SURVEY_COMPLETED = 212;
    public static final int SURVEY_CANCELLED = 213;
    private static final String TAG = "SurveyActivity";

    //SurveyKit View
    private SurveyView surveyView;

    //Progress Bar
    private ProgressBar progressBar;
    private TextView progressQuestionCount;

    private SurveyResults surveyResults;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_srvykit);
        //Don't show action bar on this activity
        getSupportActionBar().hide();

        setupUI();

        InstructionStep instructionStep = new InstructionStep("Demographics & Symptoms Survey", "To contribute towards COVID-19 research, this survey will collect some basic demographics, medical history. \nIf you agree to collecting this data press 'I agree', otherwise use the back button or back gesture to go back.", "I agree", false, new StepIdentifier("intro"));

        QuestionStep bioSexQuestion = new QuestionStep("Biological Sex", "Which is your biological sex?", "Next", new AnswerFormat.BooleanAnswerFormat(
                "Male", "Female", AnswerFormat.BooleanAnswerFormat.Result.None
        ), false, new StepIdentifier("biosex"));

        QuestionStep ageQuestion = new QuestionStep("Age", "How old are you?", "Next", new AnswerFormat.IntegerAnswerFormat(
                Integer.valueOf(0), "Age"), false, new StepIdentifier("age"));

        List<TextChoice> medicalConditions = new ArrayList<TextChoice>();
        medicalConditions.add(new TextChoice("None", "none"));
        medicalConditions.add(new TextChoice("Prefer not to say", "prefer-not-to-say"));
        medicalConditions.add(new TextChoice("Asthma", "asthma"));
        medicalConditions.add(new TextChoice("Cystic fibrosis", "cystic-fibrosis"));
        medicalConditions.add(new TextChoice("COPD/Emphysema", "copd-emphysema"));
        medicalConditions.add(new TextChoice("Pulmonary Fibrosis", "pulmonary-fibrosis"));
        medicalConditions.add(new TextChoice("Other lung disease", "other-lung-disease"));
        medicalConditions.add(new TextChoice("High Blood Pressure", "high-blood-pressure"));
        medicalConditions.add(new TextChoice("Angina", "angina"));
        medicalConditions.add(new TextChoice("Previous stroke or Transient ischaemic attack", "previous-stroke-or-transient-ischaemic-attack"));
        medicalConditions.add(new TextChoice("Previous heart attack", "previous-heart-attack"));
        medicalConditions.add(new TextChoice("Valvular heart disease", "valvular-heart-disease"));
        medicalConditions.add(new TextChoice("Other heart disease", "other-heart-disease"));
        medicalConditions.add(new TextChoice("Diabetes", "diabetes"));
        medicalConditions.add(new TextChoice("Cancer", "cancer"));
        medicalConditions.add(new TextChoice("Previous organ transplant", "previous-organ-transplant"));
        medicalConditions.add(new TextChoice("HIV or impaired immune system", "hiv-or-impaired-immune-system"));
        medicalConditions.add(new TextChoice("Other long-term condition", "other-long-term-condition"));

        QuestionStep medConditionQuestion = new QuestionStep("Medical Conditions", "Do you have any of these medical conditions? (can choose more than one)(*)", "Next", new AnswerFormat.MultipleChoiceAnswerFormat(
                medicalConditions, new ArrayList<TextChoice>()), false, new StepIdentifier("medcondition"));


        List<TextChoice> smokeChoices = new ArrayList<TextChoice>();
        smokeChoices.add(new TextChoice("Never Smoked", "never-smoked"));
        smokeChoices.add(new TextChoice("Prefer not to say", "prefer-not-to-say"));
        smokeChoices.add(new TextChoice("Ex-smoker", "ex-smoker"));
        smokeChoices.add(new TextChoice("Current smoker (less than once a day)", "current-less-than-once-daily"));
        smokeChoices.add(new TextChoice("Current smoker (1-10 cigarettes per day)", "current-one-ten-daily"));
        smokeChoices.add(new TextChoice("Current smoker (11-20 cigarettes per day)", "current-eleven-twenty-daily"));
        smokeChoices.add(new TextChoice("Current smoker (21+ cigarettes per day)", "current-21-or-more-daily"));
        smokeChoices.add(new TextChoice("Current smoker (e-cigarettes only)", "e-cig-only"));

        QuestionStep smokerQuestion = new QuestionStep("Smoking", "Do you, or have you, ever smoked (including e-cigarettes)?", "Next", new AnswerFormat.SingleChoiceAnswerFormat(smokeChoices, smokeChoices.get(0)), false, new StepIdentifier("smoking"));

        List<TextChoice> todaySymptomChoices = new ArrayList<TextChoice>();
        todaySymptomChoices.add(new TextChoice("None", "none"));
        todaySymptomChoices.add(new TextChoice("Prefer not to say", "prefer-not-to-say"));
        todaySymptomChoices.add(new TextChoice("Fever (feeling feverish or warmer than usual)", "fever"));
        todaySymptomChoices.add(new TextChoice("Chills", "chills"));
        todaySymptomChoices.add(new TextChoice("Dry cough", "dry-cough"));
        todaySymptomChoices.add(new TextChoice("Wet cough", "wet-cough"));
        todaySymptomChoices.add(new TextChoice("Difficulty breathing or feeling short of breath", "difficulty-breathing"));
        todaySymptomChoices.add(new TextChoice("Tightness in your chest", "chest-tightness"));
        todaySymptomChoices.add(new TextChoice("Loss of taste and smell", "taste-smell-loss"));
        todaySymptomChoices.add(new TextChoice("Dizziness, confusion or vertigo", "dizziness-confusion-vertigo"));
        todaySymptomChoices.add(new TextChoice("Headache", "headache"));
        todaySymptomChoices.add(new TextChoice("Muscle aches", "muscle-aches"));
        todaySymptomChoices.add(new TextChoice("Sore throat, runny or blocked nose", "sore-throat-runny-or-blocked-nose"));

        QuestionStep todaySymptomQuestionStep = new QuestionStep("How you feeling today?", "Do you have any of the following symptoms today? (can choose more than one)(*)", "Next",
                new AnswerFormat.MultipleChoiceAnswerFormat(todaySymptomChoices, new ArrayList<TextChoice>()),
                false,
                new StepIdentifier(("todaysymptom")));

        List<TextChoice> covid19results = new ArrayList<>();
        covid19results.add(new TextChoice("Never", "never"));
        covid19results.add(new TextChoice("In the last 14 days", "pos-less-than-14-days"));
        covid19results.add(new TextChoice("More than 14 days ago", "pos-more-than-14-days"));
        covid19results.add(new TextChoice("Prefer not to say", "prefer-not-to-say"));

        QuestionStep covid19ResultsQuestionStep = new QuestionStep("Positive COVID-19 Results", "Have you had a positive test for COVID-19?", "Next",
                new AnswerFormat.SingleChoiceAnswerFormat(covid19results, covid19results.get(0)),
        false,
                new StepIdentifier("covidresults"));

        List<TextChoice> hospitalStatus = new ArrayList<>();
        hospitalStatus.add(new TextChoice("No", "no"));
        hospitalStatus.add(new TextChoice("Yes", "yes"));
        hospitalStatus.add(new TextChoice("Prefer not to say", "prefer-not-to-say"));

        QuestionStep hospitalStatusQuestionStep = new QuestionStep("Hospital", "Are you in the hospital right now?", "Next",
                new AnswerFormat.SingleChoiceAnswerFormat(hospitalStatus, hospitalStatus.get(0)),
                false,
                new StepIdentifier("hospitalstatus"));

        CompletionStep completionStep = new CompletionStep("Thank you", "You have now completed the PneumoCheck COVID-19 Survey", "Finish", null, 0, false, new StepIdentifier("3"));


        //Set the order and register the steps:
        List<Step> steps = Arrays.asList(new Step[]{instructionStep, bioSexQuestion, ageQuestion, medConditionQuestion, smokerQuestion, todaySymptomQuestionStep, covid19ResultsQuestionStep, hospitalStatusQuestionStep, completionStep});

        final OrderedTask task = new OrderedTask(steps, new TaskIdentifier("1"));

        //For Custom order depending on answers use...
        //NavigableOrderedTask navigableOrderedTask = new NavigableOrderedTask(steps);

        surveyView.setOnSurveyFinish(new Function2<TaskResult, FinishReason, Unit>() {
            @Override
            public Unit invoke(final TaskResult taskResult, FinishReason finishReason) {
                if(finishReason == FinishReason.Completed){
                    surveyResults = new SurveyResults();
                    surveyResults.setSurveyState(FirestoreRepository.SurveyState.complete.toString());
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        taskResult.getResults().forEach(new Consumer<StepResult>() {
                            @Override
                            public void accept(StepResult stepResult) {
                                String questionId = stepResult.getResults().get(0).getId().getId();
                                String answer = stepResult.getResults().get(0).getStringIdentifier();
                                switch (questionId){
                                    case "age":
                                        surveyResults.setAge(answer);
                                        break;
                                    case "biosex":
                                        surveyResults.setSex(answer);
                                        break;
                                    case "medcondition":
                                        surveyResults.setMedicalConditions(answer);
                                        break;
                                    case "smoking":
                                        surveyResults.setSmoker(answer);
                                        break;
                                    case "todaysymptom":
                                        surveyResults.setTodaySymptom(answer);
                                        break;
                                    case "covidresults":
                                        surveyResults.setCovidResults(answer);
                                        break;
                                    case "hospitalstatus":
                                        surveyResults.setHospitalised(answer);
                                        break;
                                    default:
                                        Log.i(TAG, "Unrecognised :" + answer);
                                        break;
                                }
                            }
                        });
                    }
                    //Return back to calling activity
                    Intent passBackSurveyResults = new Intent();
                    passBackSurveyResults.putExtra("SurveyResults", surveyResults);
                    SurveyActivity.this.setResult(SURVEY_COMPLETED, passBackSurveyResults);
                    SurveyActivity.this.finish();
                }
                if(finishReason == FinishReason.Discarded){
                    //Return back to calling activity
                    SurveyActivity.this.setResult(SURVEY_CANCELLED);
                    SurveyActivity.this.finish();
                }
                if(finishReason == FinishReason.Saved){

                }
                if(finishReason == FinishReason.Failed){

                }
                return null;
            }
        });

        SurveyTheme surveyTheme = new SurveyTheme(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark), ContextCompat.getColor(this, R.color.colorPrimaryDark));

        surveyView.start(task, surveyTheme);

        setupUI();

    }

    private void setupUI() {
        progressBar = findViewById(R.id.progressbar_survey_questionprogress);
        progressBar.setVisibility(View.GONE);
        progressQuestionCount = findViewById(R.id.textview_survey_questioncounterprogress);
        progressQuestionCount.setVisibility(View.GONE);

        surveyView = findViewById(R.id.survey_view);
    }

}
