package com.touchmediaproductions.pneumocheck.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.touchmediaproductions.pneumocheck.MainActivity;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.CloudMLXrayContinualServerClient;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.KeyboardHelper;
import com.touchmediaproductions.pneumocheck.helpers.PictureHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.ml.PytorchMLHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.function.BiFunction;

public class EditFormActivity extends AppCompatActivity {

    public static final int CREATE_SUBMISSION_REQUEST_CODE = 100;
    private static final String TAG = "EditFormActivity";

    //Image
    private ImageView xrayPreviewThumbnailCard;
    boolean isPhotoFromGallery;
    private Uri croppedImage, fullImage;
    private Bitmap croppedImageBitMap;

    //Loading
    private ProgressBar loadingCircle;

    //Top Notification
    private CardView notificationCard;
    private TextView notificationText;

    //X-Ray Id
    private TextView xrayId;
    private String numericId;

    //InitialsBadge
    private LinearLayout initialsBadge;
    private TextView initialsTextView;

    //Form Fields
    private TextInputLayout firstNameEditTextLayout, lastNameEditTextLayout, ageEditTextLayout;
    private TableRow sexTableRow;
    private Spinner sexSpinner;
    private DatePicker scanCreationDatePicker;

    //For Keyboard dismissal purposes
    private RelativeLayout clickableRelativeLayout;

    //Prediction Card
    private TextView resultOutput, resultDetails;
    private MLHelper.Prediction prediction;

    //Prediction Confirmation or Decline Buttons
    private LinearLayout confirmationRequestButtons;
    private Button confirmPredictionButton, declinePredictionButton;
    private SubmissionModel.Confirmation predictionConfirmation;

    //End of Form Buttons
    private Button cancelButton, submitButton;

    //User Details
    private String userId;
    private FirestoreRepository.AccountType accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editformscan);
        //Don't show action bar on this activity
        getSupportActionBar().hide();

        //Unbundle the extras
        Bundle intentExtras = getIntent().getExtras();
        userId = (String) intentExtras.get("userId");
        accountType = (FirestoreRepository.AccountType) intentExtras.get("accountType");
        isPhotoFromGallery = (boolean) intentExtras.get("isGalleryPhoto");
        croppedImage = (Uri) intentExtras.get("croppedUri");
        fullImage = (Uri) intentExtras.get("fullPictureUri");

        switch (accountType) {
            case participant:
                setupUIParticipant();
                break;
            default:
                setupUI();
                break;
        }


    }

    /**
     * Link all objects to their View Ids
     * Prepare onclick methods
     */
    private void setupUIParticipant() {
        //Preview of X-Ray ImageView
        xrayPreviewThumbnailCard = findViewById(R.id.imageview_editscan_xrayimage);

        //Prepare the loading UI
        loadingCircle = findViewById(R.id.progressbar_editform_loading);

        //Load the bitmap image passed
        croppedImageBitMap = BitmapFactory.decodeFile(croppedImage.getPath());

        //Default Prediction Confirmation is Unconfirmed.
        predictionConfirmation = SubmissionModel.Confirmation.UNCONFIRMED;

        //UI Prediction Card
        resultOutput = findViewById(R.id.textView_activityeditscan_result);
        resultDetails = findViewById(R.id.textview_editform_resultinfo);
        confirmationRequestButtons = findViewById(R.id.linearlayout_editform_confirmationrequest);
        confirmPredictionButton = findViewById(R.id.btn_editform_confirmprediction);
        declinePredictionButton = findViewById(R.id.btn_editform_declineprediction);

        confirmPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPredictionConfirmation();
            }
        });
        declinePredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPredictionConfirmationWithDialog();
            }
        });

        //Top notification
        notificationCard = findViewById(R.id.card_editform_notification);
        notificationText = findViewById(R.id.textview_editform_notificationtext);

        //Xray ID
        xrayId = findViewById(R.id.textview_editform_xrayid);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        xrayId.setText("");
        numericId = String.valueOf(Long.parseLong(timeStamp));


        //Layout - For Keyboard Dissmissal Purposes
        clickableRelativeLayout = findViewById(R.id.relativelayout_editform_clickablerelativelayout);
        clickableRelativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //If the relative layout is focused, dismiss the keyboard
                if (b) {
                    closeKeyboard();
                }
            }
        });

        //Form Fields
        firstNameEditTextLayout = findViewById(R.id.textinputlayout_editform_firstname);
        lastNameEditTextLayout = findViewById(R.id.textinputlayout_editform_lastname);
        ageEditTextLayout = findViewById(R.id.textinputlayout_editform_age);
        sexSpinner = findViewById(R.id.spinner_editform_sex);
        sexTableRow = findViewById(R.id.tablerow_editform_sex);
        scanCreationDatePicker = findViewById(R.id.datepicker_editform_scancreationdate);

        firstNameEditTextLayout.setVisibility(View.GONE);
        lastNameEditTextLayout.setVisibility(View.GONE);
        ageEditTextLayout.setVisibility(View.GONE);
        sexTableRow.setVisibility(View.GONE);

        //Initials
        initialsBadge = findViewById(R.id.linearlayout_editform_initialslayout);
        initialsTextView = findViewById(R.id.textview_initials_badge);

        // Watcher will change and populate the initials badge as the text changes.
        TextWatcher initialBadgeNamesTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                populateInitialsBadge();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        //Set these fields to the TextWatcher so as the textchanges the intials badge populates (simply a cosmetic feature)
        firstNameEditTextLayout.getEditText().addTextChangedListener(initialBadgeNamesTextWatcher);
        lastNameEditTextLayout.getEditText().addTextChangedListener(initialBadgeNamesTextWatcher);


        //End Form Buttons
        submitButton = findViewById(R.id.btn_editform_submitbutton);
        cancelButton = findViewById(R.id.btn_editform_cancelbackbutton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitWithAlert();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCacheAndGoBackToSubmissions();
            }
        });

    }

    /**
     * Link all objects to their View Ids
     * Prepare onclick methods
     */
    private void setupUI() {
        //Preview of X-Ray ImageView
        xrayPreviewThumbnailCard = findViewById(R.id.imageview_editscan_xrayimage);

        //Prepare the loading UI
        loadingCircle = findViewById(R.id.progressbar_editform_loading);

        //Load the bitmap image passed
        croppedImageBitMap = BitmapFactory.decodeFile(croppedImage.getPath());

        //Default Prediction Confirmation is Unconfirmed.
        predictionConfirmation = SubmissionModel.Confirmation.UNCONFIRMED;

        //UI Prediction Card
        resultOutput = findViewById(R.id.textView_activityeditscan_result);
        resultDetails = findViewById(R.id.textview_editform_resultinfo);
        confirmationRequestButtons = findViewById(R.id.linearlayout_editform_confirmationrequest);
        confirmPredictionButton = findViewById(R.id.btn_editform_confirmprediction);
        declinePredictionButton = findViewById(R.id.btn_editform_declineprediction);

        confirmPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPredictionConfirmation();
            }
        });
        declinePredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPredictionConfirmationWithDialog();
            }
        });

        //Top notification
        notificationCard = findViewById(R.id.card_editform_notification);
        notificationText = findViewById(R.id.textview_editform_notificationtext);

        //Xray ID
        xrayId = findViewById(R.id.textview_editform_xrayid);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        xrayId.setText("X-RAYID:" + timeStamp);
        numericId = String.valueOf(Long.parseLong(timeStamp));


        //Layout - For Keyboard Dissmissal Purposes
        clickableRelativeLayout = findViewById(R.id.relativelayout_editform_clickablerelativelayout);
        clickableRelativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //If the relative layout is focused, dismiss the keyboard
                if (b) {
                    closeKeyboard();
                }
            }
        });

        //Form Fields
        firstNameEditTextLayout = findViewById(R.id.textinputlayout_editform_firstname);
        lastNameEditTextLayout = findViewById(R.id.textinputlayout_editform_lastname);
        ageEditTextLayout = findViewById(R.id.textinputlayout_editform_age);
        sexSpinner = findViewById(R.id.spinner_editform_sex);
        scanCreationDatePicker = findViewById(R.id.datepicker_editform_scancreationdate);

        //Initials
        initialsBadge = findViewById(R.id.linearlayout_editform_initialslayout);
        initialsTextView = findViewById(R.id.textview_initials_badge);

        // Watcher will change and populate the initials badge as the text changes.
        TextWatcher initialBadgeNamesTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                populateInitialsBadge();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        //Set these fields to the TextWatcher so as the textchanges the intials badge populates (simply a cosmetic feature)
        firstNameEditTextLayout.getEditText().addTextChangedListener(initialBadgeNamesTextWatcher);
        lastNameEditTextLayout.getEditText().addTextChangedListener(initialBadgeNamesTextWatcher);


        //End Form Buttons
        submitButton = findViewById(R.id.btn_editform_submitbutton);
        cancelButton = findViewById(R.id.btn_editform_cancelbackbutton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitWithAlert();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCacheAndGoBackToSubmissions();
            }
        });

    }

    /**
     * Prompt and set the ML prediction as confirmed
     */
    private void setPredictionConfirmation() {
        //Checking that prediction is null, will also catch user from causing issues by pressing buttons and the prediction is not ready.
        if (prediction != null) {
            //Prepare Dialog for prompting confirmation
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(EditFormActivity.this);
            builder.setTitle("Confirm Submit");
            builder.setMessage(getString(R.string.predictionconfirmation_agreement_confirmsubmit) + prediction.getFirst() + " as correct?")
                    .setCancelable(false)
                    .setPositiveButton(R.string.response_i_agree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //User confirmed so hide buttons
                            confirmationRequestButtons.setVisibility(View.GONE);
                            //Set the prediction
                            predictionConfirmation = SubmissionModel.Confirmation.valueOf(prediction.getFirst().replaceAll("-", "").toUpperCase());
                            //Replace where the prediction was with the confirmed diagnosis.
                            replacePredictionWithConfirmation();
                        }
                    })
                    .setNegativeButton(R.string.response_decline, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Simply dismiss the prompt.
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
    }

    /**
     * Prompt and obtain from user a correct diagnosis for the x-ray
     */
    private void setPredictionConfirmationWithDialog() {
        //Checking that prediction is null, will also catch user from causing issues by pressing buttons and the prediction is not ready.
        if (prediction != null) {
            //Start by dismissing keyboard if its showing
            closeKeyboard();

            //Prepare dialog for prompting user to provide an official diagnosis result from doctor
            final MaterialAlertDialogBuilder agreementConfirmDiagnosisDialogBuilder = new MaterialAlertDialogBuilder(EditFormActivity.this);
            agreementConfirmDiagnosisDialogBuilder.setTitle("Prediction Confirmation")
                    .setCancelable(true)
                    //With warning icon as its a serious prompt that requires consideration
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setMessage(R.string.confirmprediction_agreement_providediagnosticresult_prompt)
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //If true, make user pick a classification.
                            final String[] classfications = new String[]{"COVID-19", "Pneumonia", "Normal"};
                            AlertDialog.Builder askForTypeDialogbuilder = new AlertDialog.Builder(EditFormActivity.this, R.style.AlertDialogTheme);
                            askForTypeDialogbuilder.setTitle("Prediction Confirmation");
                            //Show warning icon
                            askForTypeDialogbuilder.setIcon(R.drawable.ic_baseline_warning_24);
                            askForTypeDialogbuilder.setSingleChoiceItems(classfications, -1, null);
                            askForTypeDialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    int choice = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                    // If the choice returned is valid
                                    if (choice > -1 && choice < classfications.length) {
                                        //Get the choice corresponding classification
                                        String choiceClassification = classfications[choice];
                                        //Set the predictionConfirmation to the classification choice
                                        predictionConfirmation = SubmissionModel.Confirmation.valueOf(choiceClassification.replaceAll("-", "").toUpperCase());
                                        //Dismiss the confirm request buttons
                                        confirmationRequestButtons.setVisibility(View.GONE);
                                        //Replace prediction with confirmed diagnosis.
                                        replacePredictionWithConfirmation();
                                    } else {
                                        //User didn't make a valid choice so prompt them to.
                                        ToastHelper.showShortToast(EditFormActivity.this, "Please select a choice.");
                                    }
                                }
                            });
                            AlertDialog askForTypeAlertDialog = askForTypeDialogbuilder.create();
                            askForTypeAlertDialog.show();
                        }
                    })
                    //If user presses Cancel simply dismiss
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            agreementConfirmDiagnosisDialogBuilder.show();

        }
    }

    /**
     * Method that reformats the resultOutput card to show a confirmed diagnosis/classification instead.
     */
    private void replacePredictionWithConfirmation() {
        String confirmedPrediction = predictionConfirmation.getDescription();
        resultOutput.setText(confirmedPrediction);
        resultOutput.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        coloriseOutput(resultOutput, confirmedPrediction);
        resultDetails.setText("CONFIRMED");
    }

    /**
     * Dismiss the keyboard using the KeyboardHelper class
     */
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        KeyboardHelper.hideSoftKeyboard(view, this);
    }

    /**
     * Show the firstname and lastname initials in the intial badge
     */
    private void populateInitialsBadge() {
        initialsBadge.setVisibility(View.VISIBLE);
        String initials = getInitials();
        if (initials.length() < 0) {
            initialsTextView.setText("");
            initialsBadge.setVisibility(View.GONE);
        } else {
            initialsTextView.setText(getInitials());
        }
    }

    /**
     * Calculate what the intials for the initials badge should be.
     *
     * @return
     */
    private String getInitials() {
        //Set both names to upper case (in case they are not) - start with whole word
        String firstNameInitial = lastNameEditTextLayout.getEditText().getText().toString().toUpperCase();
        String lastNameInitial = lastNameEditTextLayout.getEditText().getText().toString().toUpperCase();

        //If they are not empty - reduce the word to just the first letter.
        if (firstNameInitial.isEmpty()) {
            firstNameInitial = "";
        } else {
            firstNameInitial = firstNameInitial.substring(0, 1).toUpperCase();
        }
        if (lastNameInitial.isEmpty()) {
            lastNameInitial = "";
        } else {
            lastNameInitial = lastNameInitial.substring(0, 1).toUpperCase();
        }
        //Combine both and return
        return firstNameInitial + lastNameInitial;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Placing call to populate XRay image in onWindowFocusChanged
        // If this was to run on onCreate it will attempt to populate before the elements are ready.
        // Thus getting a divide by zero error on the setImageViewToResizedImage method.
        if (hasFocus && xrayPreviewThumbnailCard.getDrawable() == null) {
            //Resize and decode image to fit inside of ImageView as per Android documentation for efficient UI displaying
            PictureHelper.setImageViewToResizedImage(xrayPreviewThumbnailCard, croppedImage);
            //Run a machine learning classification on the image.
            classifyForCOVIDAndPopulateUI();
        }
    }

    /**
     * Run a classification on the image and show the result on the prediction card
     */
    private void classifyForCOVIDAndPopulateUI() {
        //Show loading UI
        loadingCircle.setVisibility(View.VISIBLE);

        //Prepare a background thread to run classification.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Moves the current Thread into the background
                // This approach reduces resource competition between the Runnable object's thread and the UI thread.
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                //Run Classification over the image and return a Prediction object containing top 3 results
//                prediction = MLHelper.runClassificationOnBitmap(EditFormActivity.this, croppedImageBitMap, MLModels.MODEL_A_COVIDNET);

                SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
                boolean allowCTScan = sharedpreferences.getBoolean("allowCTScan", false);
                boolean enableContinualAi = sharedpreferences.getBoolean("enableContinualAi", false);

                if (enableContinualAi) {
                    Log.i(TAG, "Continual AI is enabled");
                    confirmPredictionButton.setVisibility(View.GONE);
                    declinePredictionButton.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "Continual AI is disabled");
                    PytorchMLHelper pytorchMLHelper = PytorchMLHelper.getInstance();
                    //                    prediction = pytorchMLHelper.runClassificationOnBitmap(EditFormActivity.this, BitmapFactory.decodeStream(getAssets().open("normal_xray.jpg")));
                    prediction = pytorchMLHelper.runClassificationOnBitmap(EditFormActivity.this, croppedImageBitMap);

                }

                //As classification completed, tell the loadingCircle in the UI thread to disappear.
                loadingCircle.post(() -> loadingCircle.setVisibility(View.GONE));
                //Tell the output to show the result of the prediction
                resultOutput.post(() -> {
                    if (prediction == null) {
                        if (enableContinualAi) {
                            resultOutput.setText("Continual AI is enabled, results will appear after submission.");
                        }
                        return;
                    }
                    //Display result (Prediction object are in order or probability)
                    String result = prediction.getFirst();
                    resultOutput.setText(result);
                    //Color as according to classification importance
                    coloriseOutput(resultOutput, result);
                });
                //Show probabilities
                resultDetails.post(() -> {
                    if (prediction == null) {
                        return;
                    }
                    resultDetails.setVisibility(View.VISIBLE);
                    String details = prediction.toString();
                    resultDetails.setText(details);
                });
                //Make notification card disappear with animation of fading.
                notificationCard.post(() -> notificationCard.animate().alpha(0).setDuration(2000).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> notificationCard.setVisibility(View.GONE)));

            }
        }).start();
    }

    /**
     * Set the classification prediction to the appropriate color
     *
     * @param textView
     * @param result
     */
    private void coloriseOutput(TextView textView, String result) {
        //Make it red if COVID, blue if Normal and Orange if Pneumonia
        if (result.contains(getString(R.string.classification_covid_19))) {
            textView.setTextColor(Color.RED);
        } else if (result.contains(getString(R.string.classification_pneumonia))) {
            textView.setTextColor(Color.rgb(255, 165, 0));
        } else if (result.contains(getString(R.string.classification_normal))) {
            textView.setTextColor(getResources().getColor(R.color.colorPrimaryLight, getTheme()));
        }
    }

    /**
     * Remove the temporary files that the image processing creates and go back to previous activity
     */
    public void clearCacheAndGoBackToSubmissions() {
        xrayPreviewThumbnailCard.setImageDrawable(null);
        //If its from gallery don't try to erase it.
        if (!isPhotoFromGallery) {
            try {
                //Erase the redundant photos
                PictureHelper.removeCurrentImageFromPicturesFolder(this, fullImage.getPath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //Erase the cropped photo
        PictureHelper.removeCurrentImageFromPicturesFolder(this, croppedImage.getPath());

        //Set result to REFERSH submissions fragment, in order to refresh the list of submissions upon return
        this.setResult(MainActivity.REQUEST_REFRESH_SUBMISSIONS_FRAGMENT);
        this.finish();
    }

    /**
     * Commit submissions form and submit to database
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void submitSubmissionForm() {
        //If the fields are valid
        if (validateFields()) {
            SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
            boolean allowCTScan = sharedpreferences.getBoolean("allowCTScan", false);
            boolean enableContinualAi = sharedpreferences.getBoolean("enableContinualAi", false);

            BiFunction<String, String, String> runAfterImageIsUploaded = null;
            if(enableContinualAi) {
                runAfterImageIsUploaded = (submissionId, imageUrl) -> {
                    CloudMLXrayContinualServerClient.classify(submissionId);
                    return imageUrl;
                };
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Set the creation date for the submission
            Date todaysDate = new Date();
            Date dateFromDatePicker = getDateFromDatePicker(scanCreationDatePicker);

            String firstName = firstNameEditTextLayout.getEditText().getText().toString();
            String lastName = lastNameEditTextLayout.getEditText().getText().toString();
            Integer age = null;
            try {
                age = Integer.parseInt(ageEditTextLayout.getEditText().getText().toString());
            } catch (Exception ex) {
                age = 0;
            }
            String sex = sexSpinner.getSelectedItem().toString();
            byte[] pictureByteArray = PictureHelper.bitmapToByteArray(croppedImageBitMap);
            SubmissionModel.XrayType xrayType = SubmissionModel.XrayType.CXR;

            // Fake the learnt process
            Timestamp learntAt = null;
//            Timestamp learntAt = new Timestamp(todaysDate);

            //Add to database
            SubmissionModel submission = new SubmissionModel(numericId,
                    userId,
                    firstName,
                    lastName,
                    age,
                    sex,
                    dateFromDatePicker,
                    todaysDate,
                    predictionConfirmation,
                    prediction,
                    pictureByteArray,
                    xrayType,
                    learntAt);
            boolean success = FirestoreRepository.add(submission, runAfterImageIsUploaded);
            if (success) {
                ToastHelper.showLongToast(this, getString(R.string.notification_submission_added));
                clearCacheAndGoBackToSubmissions();
            } else {
                ToastHelper.showLongToast(this, getString(R.string.notification_submission_add_failed));
            }
        } else {
            ToastHelper.showShortToast(this, getString(R.string.notification_submission_invalid_wait));
        }
    }

    /**
     * Show alert upon submitting
     */
    private void submitWithAlert() {
        //Start by dismissing keyboard if its showing
        closeKeyboard();

        //Prepare agreement alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(EditFormActivity.this);
        builder.setTitle("Confirm Submit");
        builder.setMessage(R.string.promptmessage_gainagreement_submit)
                .setCancelable(false)
                .setPositiveButton(R.string.response_i_agree, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int id) {
                        //Finally Submit Form
                        submitSubmissionForm();
                    }
                })
                .setNegativeButton(R.string.response_decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Validate all fields are filled in and valid.
     *
     * @return
     */
    private boolean validateFields() {

        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        boolean allowCTScan = sharedpreferences.getBoolean("allowCTScan", false);
        boolean enableContinualAi = sharedpreferences.getBoolean("enableContinualAi", false);

        boolean userDetailsValid = true;
        //If user is not a participant, then validate all fields. (we already have participant details)
        if (accountType != FirestoreRepository.AccountType.participant) {
            boolean isFirstNameValid = !firstNameEditTextLayout.getEditText().getText().toString().isEmpty();

            if (!isFirstNameValid) {
                //Use the setError method to have the UI show the ! error and select the field red
                firstNameEditTextLayout.setError("Please enter first name");
            }

            boolean isLastNameValid = !lastNameEditTextLayout.getEditText().getText().toString().isEmpty();

            if (!isLastNameValid) {
                lastNameEditTextLayout.setError("Please enter first name");
            }

            boolean isAgeValid = !ageEditTextLayout.getEditText().getText().toString().isEmpty();

            if (!isAgeValid) {
                ageEditTextLayout.setError("Please enter valid age");
            }

            userDetailsValid = isFirstNameValid && isLastNameValid && isAgeValid;
        }

        // If continualAI is enabled, no prediction is valid
        boolean isPredictionValid = enableContinualAi || (prediction != null && !prediction.getFirst().isEmpty() && !prediction.getSecond().isEmpty() && !prediction.getThird().isEmpty());
        boolean isCroppedImageValid = croppedImageBitMap != null;

        if (userDetailsValid && isPredictionValid && isCroppedImageValid) {
            return true;
        }
        return false;
    }

    /**
     * Get the date from the date picker that the user has set to.
     *
     * @param datePicker
     * @return
     */
    public static Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

}