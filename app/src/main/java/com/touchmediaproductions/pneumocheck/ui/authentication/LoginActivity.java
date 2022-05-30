package com.touchmediaproductions.pneumocheck.ui.authentication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.touchmediaproductions.pneumocheck.MainActivity;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.helpers.ValidationHelper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";

    //Firebase
    private FirebaseAuth firebaseAuth;

    //Fields
    private TextInputLayout editUsernameInputLayout;
    private TextInputLayout editPasswordInputLayout;

    //Buttons
    private Button btnSignIn;
    private Button btnSignUp;
    private Button btnForgotPassword;

    //Loading
    private ProgressBar loadingSpinner;

    //Response
    private ImageView response;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Don't show action bar on this activity
        getSupportActionBar().hide();

        setupUI();
    }

    private void setupUI() {
        firebaseAuth = FirebaseAuth.getInstance();

        editUsernameInputLayout = findViewById(R.id.textinputlayout_login_emailaddress);
        editUsernameInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editUsernameInputLayout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editPasswordInputLayout = findViewById(R.id.textinputlayout_login_password);
        editPasswordInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editPasswordInputLayout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        btnSignIn = findViewById(R.id.button_login_signin);
        btnSignUp = findViewById(R.id.button_login_signup);
        btnForgotPassword = findViewById(R.id.button_login_forgotpassword);
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestEmailToSendPasswordResetLink();
            }
        });

        loadingSpinner = findViewById(R.id.progressbar_login_loading);

        response = findViewById(R.id.imageview_login_response);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSignIn();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                userSignUp();
            }
        });

    }

    private void requestEmailToSendPasswordResetLink(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Password Reset Link");
        builder.setBackground(getResources().getDrawable(R.drawable.dialog_background, getTheme()));
        builder.setIcon(R.drawable.ic_baseline_email_24);
        builder.setMessage("Enter your login email address and a password reset link will be sent.");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.fragmentdialog_resetpassword, (ViewGroup) getWindow().getDecorView().getRootView(), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.edittext_resetpassword_email);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);
        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Get the email entered:
                String emailLoginToSendPasswordResetLink = input.getText().toString();
                //Validate the email:
                if(ValidationHelper.isEmailValid(emailLoginToSendPasswordResetLink)) {
                    //Send password reset link:
                    sendResetPasswordLink(emailLoginToSendPasswordResetLink);
                } else {
                    ToastHelper.showShortToast(LoginActivity.this, "Email is invalid.");
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void sendResetPasswordLink(String userLoginEmail) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(userLoginEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ToastHelper.showShortToast(LoginActivity.this, "Password Reset Link Email Sent");
                            Log.d(TAG, "Email sent.");
                        } else if (task.isCanceled()){
                            ToastHelper.showShortToast(LoginActivity.this, "Password Reset Link Email Sent");
                        }
                    }
                });
    }

    private void userSignUp() {
        Intent startSignUpActivity = new Intent(this, UserSignUpActivity.class);
        startActivityForResult(startSignUpActivity, UserSignUpActivity.CREATE_USER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case UserSignUpActivity.CREATE_USER:
                if(resultCode == UserSignUpActivity.USER_CREATED_SUCCESS && data != null) {
                    //User was created and returned here. (Extract user's details to populate login field and log user in).
                    Bundle returnedData = data.getExtras();
                    String emailUsername = (String) returnedData.get("emailUsername");

                    editUsernameInputLayout.getEditText().setText(emailUsername);
                    editPasswordInputLayout.setEndIconActivated(false);

                } else if (resultCode == UserSignUpActivity.USER_CREATED_CANCELLED){
                    ToastHelper.showShortToast(LoginActivity.this, "User sign up and creation failed. Please try again.");
                } else if (resultCode == UserSignUpActivity.USER_CREATED_FAILED_COLLISION && data != null){
                    ToastHelper.showShortToast(LoginActivity.this, "User already exists. Please try to login with the email provided instead.");
                    Bundle returnedData = data.getExtras();
                    String emailUsername = (String) returnedData.get("emailUsername");
                    editUsernameInputLayout.getEditText().setText(emailUsername);
                    editPasswordInputLayout.setEndIconActivated(false);
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        //If user is logged in, move on.
        continueToNextActivity(currentUser);
        editPasswordInputLayout.getEditText().setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void continueToNextActivity(FirebaseUser currentUser){
        if(currentUser != null) {
            //Change UI to reflect user logged in.
            Intent changeToMainActivity = new Intent(this, MainActivity.class);
            changeToMainActivity.putExtra("loggedInUserParcelable", currentUser);
            startActivity(changeToMainActivity);
        }
    }

    private void userSignIn(){
        response.setVisibility(View.GONE);

        String email = editUsernameInputLayout.getEditText().getText().toString();
        String password = editPasswordInputLayout.getEditText().getText().toString();

        if(fieldsContainValidData(editUsernameInputLayout, editPasswordInputLayout)) {
            //Show loading
            loadingSpinner.setVisibility(View.VISIBLE);
            //Sign in attempt
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                //Clear password field
                                editPasswordInputLayout.getEditText().setText("");
                                //Move on as user is authenticated.
                                continueToNextActivity(user);
                                showPositiveResponse();
                            } else {

                                /**
                                 * signInWithEmailAndPassword throws two exceptions:
                                 *
                                 * FirebaseAuthInvalidUserException: if email doesn't exist or disabled.
                                 * FirebaseAuthInvalidCredentialsException: if password is wrong
                                 */

                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                ToastHelper.showShortToast(LoginActivity.this, "Authentication failed.");
                                continueToNextActivity(null);
                                showNegativeResponse();
                            }

                            loadingSpinner.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadingSpinner.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
        }
    }

    private boolean fieldsContainValidData(TextInputLayout usernameInputLayout, TextInputLayout passwordInputLayout) {
        boolean allValid = true;

        usernameInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);

        String username = usernameInputLayout.getEditText().getText().toString();

        if(!ValidationHelper.isEmailValid(username)){
            usernameInputLayout.setError("Please enter a valid username.");
            usernameInputLayout.setHint("example@email.com");
            allValid = false;
        }

        String password = passwordInputLayout.getEditText().getText().toString();
        boolean isPasswordEmpty = password.isEmpty();

        if(isPasswordEmpty){
            passwordInputLayout.setError("Please enter a valid password.");
            allValid = false;
        }

        return allValid;
    }

    private void showPositiveResponse(){
        showResponse(R.drawable.ic_baseline_check_24);
    }

    private void showNegativeResponse(){
        showResponse(R.drawable.ic_baseline_cross_24);
    }

    private void showResponse(final int resId){
        response.post(new Runnable() {
            @Override
            public void run() {
                response.setAlpha(1.0f);
                response.setVisibility(View.VISIBLE);
                response.setImageResource(resId);
                response.animate().alpha(0).setDuration(3000).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        response.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

}
