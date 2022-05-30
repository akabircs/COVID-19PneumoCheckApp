package com.touchmediaproductions.pneumocheck;

import android.content.Context;
import android.view.View;
import android.view.autofill.AutofillManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.touchmediaproductions.pneumocheck.ui.authentication.UserSignUpActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SignUpActivityTest {

    private View decorView;

    @Rule
    public ActivityScenarioRule<UserSignUpActivity> activityRule
            = new ActivityScenarioRule<>(UserSignUpActivity.class);

    @Before
    public void signOutIfSignedIn(){
        try {
            onView(withId(R.id.navigation_settings))
                    .perform(click());
            onView(withId(R.id.button_settings_logout))
                    .perform(click());
        } catch (Exception ex){

        }
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AutofillManager autofillManager = appContext.getSystemService(AutofillManager.class);
        autofillManager.disableAutofillServices();
        if (autofillManager != null) {
            autofillManager.cancel();
        }
    }

    @Test
    public void signUpFormNoAccountTypeChosenShowsErrorTest(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView("firstName lastName"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView("test@test.com"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_passwordanswer)).perform(click()).perform(typeTextIntoFocusedView("P@55word1"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.edittext_signupform_passwordverifyanswer)).perform(click()).perform(typeTextIntoFocusedView("P@55word1"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.button_signupform_next)).perform(click());

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<UserSignUpActivity>() {
            @Override
            public void perform(UserSignUpActivity activity) {
                decorView = activity.getWindow().getDecorView();
            }
        });

        onView(withText("Please choose an account type."))
                .inRoot(withDecorView(not(decorView)))// Here we use decorView
                .check(matches(isDisplayed()));
    }



}
