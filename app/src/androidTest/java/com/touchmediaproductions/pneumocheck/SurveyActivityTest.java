package com.touchmediaproductions.pneumocheck;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.touchmediaproductions.pneumocheck.survey.SurveyActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SurveyActivityTest {

    @Rule
    public ActivityScenarioRule<SurveyActivity> activityRule
            = new ActivityScenarioRule<>(SurveyActivity.class);

    @Before
    public void signOutIfSignedIn(){

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void fastRunThroughSurveyTest(){
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_continue), withText("I agree"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction radioButton = onView(
                allOf(withText("Male"),
                        childAtPosition(
                                allOf(withId(R.id.singleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                0)));
        radioButton.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.textFieldPartField), withText("0"),
                        childAtPosition(
                                allOf(withId(R.id.integerFieldPartField),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                0)));
        editText.perform(scrollTo(), click());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.textFieldPartField), withText("0"),
                        childAtPosition(
                                allOf(withId(R.id.integerFieldPartField),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                0)));
        editText2.perform(scrollTo(), replaceText("25"));

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.textFieldPartField), withText("25"),
                        childAtPosition(
                                allOf(withId(R.id.integerFieldPartField),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                0),
                        isDisplayed()));
        editText3.perform(closeSoftKeyboard());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withText("Prefer not to say"),
                        childAtPosition(
                                allOf(withId(R.id.multipleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                1)));
        checkBox.perform(scrollTo(), click());

        ViewInteraction checkBox2 = onView(
                allOf(withText("Other lung disease"),
                        childAtPosition(
                                allOf(withId(R.id.multipleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                6)));
        checkBox2.perform(scrollTo(), click());

        ViewInteraction checkBox3 = onView(
                allOf(withText("Angina"),
                        childAtPosition(
                                allOf(withId(R.id.multipleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                8)));
        checkBox3.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.button_continue)));
        appCompatButton4.perform(click());

        ViewInteraction radioButton2 = onView(
                allOf(withText("Current smoker (11-20 cigarettes per day)"),
                        childAtPosition(
                                allOf(withId(R.id.singleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                5)));
        radioButton2.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton5.perform(click());

        ViewInteraction checkBox4 = onView(
                allOf(withText("Prefer not to say"),
                        childAtPosition(
                                allOf(withId(R.id.multipleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                1)));
        checkBox4.perform(scrollTo(), click());

        ViewInteraction checkBox5 = onView(
                allOf(withText("Difficulty breathing or feeling short of breath"),
                        childAtPosition(
                                allOf(withId(R.id.multipleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                6)));
        checkBox5.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction radioButton3 = onView(
                allOf(withText("More than 14 days ago"),
                        childAtPosition(
                                allOf(withId(R.id.singleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                2)));
        radioButton3.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction radioButton4 = onView(
                allOf(withText("Yes"),
                        childAtPosition(
                                allOf(withId(R.id.singleChoicePart),
                                        childAtPosition(
                                                withId(R.id.content_container),
                                                2)),
                                1)));
        radioButton4.perform(scrollTo(), click());

        onView(withId(R.id.survey_view))
                .perform(swipeUp());

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.button_continue), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton8.perform(click());

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.button_continue), withText("Finish"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.questionFooter),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton9.perform(click());
    }
}
