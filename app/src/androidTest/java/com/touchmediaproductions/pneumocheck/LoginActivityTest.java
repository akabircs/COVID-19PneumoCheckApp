package com.touchmediaproductions.pneumocheck;

import android.content.Context;
import android.os.SystemClock;
import android.view.autofill.AutofillManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.touchmediaproductions.pneumocheck.ui.authentication.LoginActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    final String testEmail1 = "test@test.com";
    final String testPassword1 = "P@55word1";
    final String testFirstNameAndLastName = "John AppleSeed";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule
            = new ActivityScenarioRule<>(LoginActivity.class);

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



//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertEquals("com.touchmediaproductions.pneumocheck", appContext.getPackageName());
//    }

    @Test
    public void signInTest(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        onView(withId(R.id.edittext_login_emailaddress))
                .perform(click()).perform(typeTextIntoFocusedView("test@test.com"));
        onView(withId(R.id.edittext_login_password)).perform(click()).perform(typeTextIntoFocusedView("test1234"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.button_login_signin)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.navigation_submissions)).check(matches(isDisplayed()));
    }

    @Test
    public void signUpButtonOpensActivityFillsAndReturnsBackToSignInTest(){
        onView(withId(R.id.button_login_signup)).perform(click());

        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView(testFirstNameAndLastName));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView(testEmail1));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_passwordanswer)).perform(click()).perform(typeTextIntoFocusedView(testPassword1));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.edittext_signupform_passwordverifyanswer)).perform(click()).perform(typeTextIntoFocusedView(testPassword1));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());

        final String[] firstAccountType = new String[1];

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<LoginActivity>() {
            @Override
            public void perform(LoginActivity activity) {
                firstAccountType[0] = activity.getResources().getStringArray(R.array.display_account_types)[0];

            }
        });

        onView(withText(firstAccountType[0])).perform(click());

        onView(withId(R.id.button_signupform_next)).perform(click());

        onView(withId(R.id.button_signupform_submit)).perform(click());

        onView(withId(R.id.button_login_signin)).check(matches(isDisplayed()));
    }

    @Test
    public void signUpFillingInUserLoginActivityUserNameAndPasswordFieldsAfterSignUpTest(){

        onView(withId(R.id.button_login_signup)).perform(click());

        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView(testFirstNameAndLastName));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_shortanswer)).perform(click()).perform(typeTextIntoFocusedView(testEmail1));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());
        onView(withId(R.id.edittext_signupform_passwordanswer)).perform(click()).perform(typeTextIntoFocusedView(testPassword1));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.edittext_signupform_passwordverifyanswer)).perform(click()).perform(typeTextIntoFocusedView(testPassword1));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.button_signupform_next)).perform(click());

        final String[] firstAccountType = new String[1];

        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<LoginActivity>() {
            @Override
            public void perform(LoginActivity activity) {
                firstAccountType[0] = activity.getResources().getStringArray(R.array.display_account_types)[0];
            }
        });

        onView(withText(firstAccountType[0])).perform(click());

        onView(withId(R.id.button_signupform_next)).perform(click());

        onView(withId(R.id.button_signupform_submit)).perform(click());

        //Assert that the fields Match the signup data
        onView(withId(R.id.edittext_login_emailaddress)).check(matches(withText(testEmail1)));
        onView(withId(R.id.edittext_login_password)).check(matches(withText(testPassword1)));
    }

}