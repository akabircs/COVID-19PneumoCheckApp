package com.touchmediaproductions.pneumocheck.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.helpers.ValidationHelper;

public class UserSignUpActivity extends AppCompatActivity {

    public static final int CREATE_USER = 111;
    public static final int USER_CREATED_SUCCESS = 112;
    public static final int USER_CREATED_CANCELLED = 113;
    public static final int USER_CREATED_FAILED_COLLISION = 114;
    public static final int USER_CREATED_FAILED = 115;
    private static final int TOTALQUESTIONCOUNT = 4;

    private static final String TAG = "SignUp";

    //Firebase
    private FirebaseAuth firebaseAuth;


    //Form Title
    private TextView formTitleTextView;
    //Form ProgressBar
    private ProgressBar progressBarIndicator;
    //Form Counter Indicator
    private TextView questionCounterProgressTextView;


    //Heading or Question
    private TextView questionHeaderTextView;

    //Description or Body
    private TextView descriptionTextView;

    //Radio Question
    private MaterialCardView radioQuestionCardView;
    private RadioGroup radioQuestionRadioGroup;

    //Text Question Card
    private MaterialCardView shortTextQuestionCardView;
    private TextInputLayout shortTextAnswerInputLayout;

    //Password Question Card
    private MaterialCardView passwordQuestionCardView;
    private TextInputLayout passwordAnswerInputLayout;
    private TextInputLayout passwordVerifyAnswerInputLayout;
    private ImageView passwordMatchImageView;

    //Boolean Button Question Card
    private MaterialCardView booleanButtonQuestionCardView;


    //Next Button
    private MaterialButton nextButton;
    //Submit Button
    private MaterialButton submitButton;


    //New Account Details
    private String name = null;
    private String emailUsername = null;
    private String password = null;
    private FirestoreRepository.AccountType accountType = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_original);
        //Don't show action bar on this activity
        getSupportActionBar().hide();

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        setupUI();
        resetView();
        beginFirstRequest();
    }

    private void beginFirstRequest(){
        requestFullName();
    }

    private void requestFullName() {
        resetView();
        increaseProgressBar(1, TOTALQUESTIONCOUNT);
        showShortTextQuestion("Full Name", "Please enter your full name.", "First & Last Name", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = shortTextAnswerInputLayout.getEditText().getText().toString();
                if(!name.isEmpty()){
                    requestEmail();
                }
            }
        }, InputType.TYPE_TEXT_VARIATION_PERSON_NAME|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    }

    private void requestEmail(){
        resetView();
        increaseProgressBar(2, TOTALQUESTIONCOUNT);
        showShortTextQuestion("Email", "Please enter your email address", "Email", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailUsername = shortTextAnswerInputLayout.getEditText().getText().toString();
                if(ValidationHelper.isEmailValid(emailUsername)){
                    requestPassword();
                } else {
                    shortTextAnswerInputLayout.setError("Please enter a valid email address.");
                }
            }
        }, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    private void requestPassword(){
        resetView();
        increaseProgressBar(3, TOTALQUESTIONCOUNT);
        showPasswordQuestion("Password", "Please create a secure password that is at least 8 characters long and contains at least:\n• One upper case character\n• One lower case character\n• One number\n• One special character", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String proposedPassword = "";
                String proposedPasswordVerify = "";
                proposedPassword = passwordAnswerInputLayout.getEditText().getText().toString();
                proposedPasswordVerify = passwordVerifyAnswerInputLayout.getEditText().getText().toString();
                if(proposedPassword.isEmpty() || !proposedPassword.matches("(?=^.{8,}$)(?=.*\\d)(?=.*[!@#$%^&*]+)(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")){
                    passwordAnswerInputLayout.setError("Please enter a valid password");
                } else if(!proposedPassword.contentEquals(proposedPasswordVerify)){
                    passwordVerifyAnswerInputLayout.setError("Passwords do not match. Please enter the same password in both fields.");
                } else {
                    password = proposedPassword;
                    requestAccountType();
                }
            }
        });
    }


    private void requestAccountType(){
        resetView();
        increaseProgressBar(TOTALQUESTIONCOUNT, TOTALQUESTIONCOUNT);
        showRadioQuestion("Account Type", "Please choose an account type", getResources().getStringArray(R.array.display_account_types), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkedId = radioQuestionRadioGroup.getCheckedRadioButtonId();
                RadioButton checkedButton = radioQuestionRadioGroup.findViewById(checkedId);
                if(checkedButton != null) {
                    String selectedAccountType = checkedButton.getText().toString();
                    try {
                        accountType = FirestoreRepository.AccountType.valueOf(selectedAccountType.toLowerCase());
                    } catch (Exception ex){
                        accountType = FirestoreRepository.AccountType.participant;
                        Log.e(TAG, "Account type not recognised" + selectedAccountType.toLowerCase());
                    }
                }
                if(accountType != null) {
                    requestSignUpComplete();
                } else {
                    ToastHelper.showShortToast(UserSignUpActivity.this, "Please choose an account type.");
                }
            }
        });
    }

    private void requestSignUpComplete(){
        resetView();
        increaseProgressBar(1, 1);
        showCompleteConfirmation("Finish", "Press submit to finish creating your account.", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commitUserToCreate();
            }
        });
    }

    private void showRadioQuestion(String question, String description, String[] options, View.OnClickListener onClickListener) {
        questionHeaderTextView.setText(question);
        questionHeaderTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setText(description);
        descriptionTextView.setVisibility(View.VISIBLE);
        radioQuestionCardView.setVisibility(View.VISIBLE);

        //Iterate through the given array of options
        for (int i = 0; i < options.length; i++) {
            RadioButton radioButton = new RadioButton(UserSignUpActivity.this);
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT, 1f);
            //Prepare padding and margin dimensions
            int dp8 = (int) DimensionHelper.dpTopixel(UserSignUpActivity.this, 8);
            int dp16 = (int) DimensionHelper.dpTopixel(UserSignUpActivity.this, 16);
            layoutParams.setMargins(dp16, dp16, dp16, dp16);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
            radioButton.setButtonDrawable(R.drawable.radiobutton_persontoggle_selector);
            //Set Padding
            radioButton.setPadding(dp8, dp8, dp8, dp8);
            //Get system to assign an id to the button view
            radioButton.setId(View.generateViewId());
            radioButton.setText(options[i]);
            radioQuestionRadioGroup.addView(radioButton);
        }

        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(onClickListener);
    }

    private void showShortTextQuestion(String question, String description, String hint, View.OnClickListener onClickListener, int inputType){
        questionHeaderTextView.setText(question);
        questionHeaderTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setText(description);
        descriptionTextView.setVisibility(View.VISIBLE);
        shortTextQuestionCardView.setVisibility(View.VISIBLE);
        shortTextAnswerInputLayout.setHint(hint);
        shortTextAnswerInputLayout.getEditText().setText("");
        shortTextAnswerInputLayout.getEditText().setInputType(inputType);

        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(onClickListener);
    }

    private void showPasswordQuestion(String question, String description, View.OnClickListener onClickListener){
        questionHeaderTextView.setText(question);
        questionHeaderTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setText(description);
        descriptionTextView.setVisibility(View.VISIBLE);

        passwordQuestionCardView.setVisibility(View.VISIBLE);
        passwordAnswerInputLayout.getEditText().setText("");
        passwordAnswerInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordAnswerInputLayout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        passwordVerifyAnswerInputLayout.getEditText().setText("");
        passwordVerifyAnswerInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordVerifyAnswerInputLayout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                //If verify field matches password Answer field then show a checkmark
                String passwordText = passwordAnswerInputLayout.getEditText().getText().toString();
                String verifyPasswordText = passwordVerifyAnswerInputLayout.getEditText().getText().toString();
                if(!passwordText.isEmpty() && passwordText.contentEquals(verifyPasswordText)){
                    passwordMatchImageView.setVisibility(View.VISIBLE);
                } else {
                    passwordMatchImageView.setVisibility(View.GONE);
                }
            }
        });

        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(onClickListener);
    }

    private void showCompleteConfirmation(String header, String body, View.OnClickListener onClickListener){
        questionHeaderTextView.setText(header);
        questionHeaderTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setText(body);
        descriptionTextView.setVisibility(View.VISIBLE);

        nextButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
        submitButton.setOnClickListener(onClickListener);
    }

    private void setupUI() {
        formTitleTextView = findViewById(R.id.textview_signupform_title);
        progressBarIndicator = findViewById(R.id.progressbar_signupform_questionprogress);
        questionCounterProgressTextView = findViewById(R.id.textview_signupform_questioncounterprogress);
        questionHeaderTextView = findViewById(R.id.textview_signupform_question);
        descriptionTextView = findViewById(R.id.textview_signupform_description);

        radioQuestionCardView = findViewById(R.id.materialcard_signup_radioquestioncard);
        radioQuestionRadioGroup = findViewById(R.id.radiogroup_signupform_optionsGroup);

        shortTextQuestionCardView = findViewById(R.id.materialcard_signupform_shorttextquestioncard);
        shortTextAnswerInputLayout = findViewById(R.id.textinputlayout_signup_shortanswerinput);

        passwordQuestionCardView = findViewById(R.id.materialcard_signupform_passwordquestioncard);
        passwordAnswerInputLayout = findViewById(R.id.textinputlayout_signup_passwordinput);
        passwordVerifyAnswerInputLayout = findViewById(R.id.textinputlayout_signup_passwordverifyinput);
        passwordMatchImageView = findViewById(R.id.imageview_signupform_passwordsmatch);

        booleanButtonQuestionCardView = findViewById(R.id.materialcard_signupform_booleanbuttonquestioncard);

        nextButton = findViewById(R.id.button_signupform_next);
        submitButton = findViewById(R.id.button_signupform_submit);

        initiateProgressBar();
    }

    private void resetView(){
        questionHeaderTextView.setVisibility(View.GONE);
        descriptionTextView.setVisibility(View.GONE);
        radioQuestionCardView.setVisibility(View.GONE);
        shortTextQuestionCardView.setVisibility(View.GONE);
        passwordQuestionCardView.setVisibility(View.GONE);
        booleanButtonQuestionCardView.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    private void initiateProgressBar(){
        progressBarIndicator.setMax(100);
        progressBarIndicator.setProgress(0);
    }

    private void increaseProgressBar(int questionNumber, int totalQuestions){
        progressBarIndicator.incrementProgressBy(100 / totalQuestions);
        questionCounterProgressTextView.setText(questionNumber + "/" + totalQuestions);
    }

    private void signUpNewUser(String emailUsername, String password, final FirestoreRepository.AccountType accountType, final String displayName){
        firebaseAuth.createUserWithEmailAndPassword(emailUsername, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //Add displayname to the user profile:
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                            user.updateProfile(profileUpdates);

                            //Use this UID to create a firestore profile
                            String uniqueUserID = user.getUid();
                            //Create user profile too.
                            FirestoreRepository.createUserProfile(uniqueUserID, displayName, accountType);

                            finishAndReturnToLogin(USER_CREATED_SUCCESS);

                        } else {
                            Log.i(TAG, "createUserWithEmail:failure >> " + task.getException().getMessage());

                            /**
                             * createUserWithEmailAndPassword throws 3 exceptions:
                             *
                             * FirebaseAuthWeakPasswordException: if the password is not strong enough
                             * FirebaseAuthInvalidCredentialsException: if the email address is malformed
                             * FirebaseAuthUserCollisionException: if there already exists an account with the given email address.
                             *
                             */

                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                finishAndReturnToLogin(USER_CREATED_FAILED_COLLISION);
                            } else {
                                finishAndReturnToLogin(USER_CREATED_CANCELLED);
                            }
                        }
                    }
                });
    }

    private void commitUserToCreate(){
        signUpNewUser(this.emailUsername, this.password, this.accountType, this.name);
    }

    private void finishAndReturnToLogin(int resultCode){
        Intent newUserIntent = null;
        if(resultCode == USER_CREATED_SUCCESS) {
            newUserIntent = new Intent();
            newUserIntent.putExtra("emailUsername", emailUsername);
            newUserIntent.putExtra("password", password);
        } else if (resultCode == USER_CREATED_FAILED_COLLISION) {
            newUserIntent = new Intent();
            newUserIntent.putExtra("emailUsername", emailUsername);
        } else if (resultCode == USER_CREATED_CANCELLED) {
        }
        UserSignUpActivity.this.setResult(resultCode, newUserIntent);
        UserSignUpActivity.this.finish();
    }

}
