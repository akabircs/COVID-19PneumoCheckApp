package com.touchmediaproductions.pneumocheck.ui.submissions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.CloudMLXrayContinualServerClient;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.KeyboardHelper;
import com.touchmediaproductions.pneumocheck.helpers.PictureHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;

import java.io.Serializable;
import java.util.Date;

/**
 * DialogFragment that populates its UI from the SubmissionModel that the user is about to edit and then saves the edits back into the submission model to be returned.
 */
public class UpdateSubmissionFragmentDialog  extends DialogFragment {

    public static final int DIALOG_UPDATE_SUBMISSION_REQUEST_CODE = 100;
    private static final String TAG = "UpdateSubmissionFragmentDialog";


    private SubmissionModel submissionToEdit;
    private int submissionPosition;

    //Keyboard Dismissal Purposes
    private RelativeLayout clickableRelativeLayout;

    //Form Fields
    private EditText firstnameField;
    private EditText lastnameField;
    private EditText ageField;
    private AppCompatSpinner sexField;

    //Form Displays
    private ImageView xrayPreviewThumbnail;
    private TextView xRayIdDisplay;
    private TextView submissionCreationDateField;

    //Prediction Card
    private TextView resultOutput, resultDetails;
    private MLHelper.Prediction prediction;
    //Prediction Buttons
    private LinearLayout confirmationRequestButtons;
    private Button confirmPredictionButton, declinePredictionButton;
    private SubmissionModel.Confirmation predictionConfirmation;

    //End of Form Button
    private Button cancel, submit;


    public static UpdateSubmissionFragmentDialog newInstance(int position, SubmissionModel submission) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        UpdateSubmissionFragmentDialog fragment = new UpdateSubmissionFragmentDialog();
        fragment.setArguments(args);
        fragment.setSubmission(submission);
        return fragment;
    }

    public void setSubmission(SubmissionModel submission){
        this.submissionToEdit = submission;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_updatesubmission, container, false);

        // Set transparent background and no title - in order to allow the rounded corners of the parent view to show
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams wmlp = getDialog().getWindow().getAttributes();
            wmlp.gravity = Gravity.FILL_HORIZONTAL;
        }

        this.setCancelable(false);

        submissionPosition = (int) getArguments().getInt("position");

        xrayPreviewThumbnail = root.findViewById(R.id.imageview_updatesubmission_xrayimage);
        xRayIdDisplay = root.findViewById(R.id.textview_updatesubmission_xrayid);

        firstnameField = root.findViewById(R.id.edittext_updatesubmission_firstname);
        lastnameField = root.findViewById(R.id.edittext_updatesubmission_lastname);
        ageField = root.findViewById(R.id.edittext_updatesubmission_age);

        sexField = root.findViewById(R.id.spinner_updatesubmission_sex);

        submissionCreationDateField = root.findViewById(R.id.textview_updatesubmission_scancreationdate_viewonly);

        confirmationRequestButtons = root.findViewById(R.id.linearlayout_updatesubmission_confirmationrequest);
        confirmPredictionButton = root.findViewById(R.id.btn_updatesubmission_confirmprediction);
        declinePredictionButton = root.findViewById(R.id.btn_updatesubmission_declineprediction);
        resultOutput = root.findViewById(R.id.textView_updatesubmission_result);
        resultDetails = root.findViewById(R.id.textview_updatesubmission_resultinfo);

        cancel = root.findViewById(R.id.btn_updatesubmission_cancelbackbutton);
        submit = root.findViewById(R.id.btn_updatesubmission_submitbutton);

        if(submissionToEdit != null) {
            Bitmap cxrBitmap = PictureHelper.byteArrayToBitmap(submissionToEdit.getCxrPhoto());
            xrayPreviewThumbnail.setImageBitmap(cxrBitmap);

            xRayIdDisplay.setText("X-RAYID:"+ submissionToEdit.getId());
            firstnameField.setText(submissionToEdit.getFirstName());
            lastnameField.setText(submissionToEdit.getLastName());
            ageField.setText(String.valueOf(submissionToEdit.getAge()));

            //Set Selection of spinner position by first querying the position of the sex string in the adapter.
            sexField.setSelection(((ArrayAdapter<String>) sexField.getAdapter()).getPosition(submissionToEdit.getSex()));

            submissionCreationDateField.setText(submissionToEdit.getScanCreationDate().toString());

            prediction = submissionToEdit.getPrediction();
            if(prediction != null){
                String highestPrediction = prediction.getFirst();
                String predictionDetails = prediction.toString();
                resultOutput.setText(highestPrediction);
                coloriseOutput(resultOutput, highestPrediction);
                resultDetails.setText(predictionDetails);
                resultDetails.setVisibility(View.VISIBLE);
                confirmPredictionButton.setVisibility(View.VISIBLE);
                declinePredictionButton.setVisibility(View.VISIBLE);
                confirmationRequestButtons.setVisibility(View.VISIBLE);
            } else {
                resultDetails.setVisibility(View.GONE);
                confirmPredictionButton.setVisibility(View.GONE);
                declinePredictionButton.setVisibility(View.GONE);
                confirmationRequestButtons.setVisibility(View.GONE);
            }
            //Initiate Prediction Confirmation with Value from submission to edit
            predictionConfirmation = submissionToEdit.getConfirmation();
            if(predictionConfirmation != null && predictionConfirmation != SubmissionModel.Confirmation.UNCONFIRMED){
                resultOutput.setText(predictionConfirmation.getDescription());
                resultOutput.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                resultDetails.setText("CONFIRMED");
                confirmPredictionButton.setVisibility(View.GONE);
                declinePredictionButton.setVisibility(View.GONE);
                confirmationRequestButtons.setVisibility(View.GONE);
            }

        } else {
            Toast.makeText(getContext(), "Submission To Edit was null", Toast.LENGTH_SHORT).show();
        }

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SubmissionModel updatedSubmission = saveAllIntoNewSubmission();

                Log.i("SUBMISSION", updatedSubmission.toString());

                if(updatedSubmission != null) {

                    // Update Firestore
                    FirestoreRepository.updateSubmission(updatedSubmission.getId(), updatedSubmission);

                    // Teach continual AI if enabled.
                    ifContinualAITeachModel(updatedSubmission.getId(), predictionConfirmation);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("updatedSubmission", updatedSubmission);
                    bundle.putInt("position", submissionPosition);
                    Intent intent = new Intent().putExtras(bundle);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    dismissAllowingStateLoss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });

        confirmPredictionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setPredictionConfirmation();
            }
        });

        declinePredictionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setPredictionConfirmationWithDialog();
            }
        });

        //Layout - For Keyboard Dissmissal Purposes
        clickableRelativeLayout = root.findViewById(R.id.relativelayout_updatesubmission_clickablerelativelayout);
        clickableRelativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //If the relative layout is focused, dismiss the keyboard
                if(b){
                    closeKeyboard();
                }
            }
        });

        return root;
    }

    private SubmissionModel saveAllIntoNewSubmission(){
        if(validateFields()) {
            String xRayID = submissionToEdit.getId();
            String firstName = firstnameField.getText().toString();
            String lastName = lastnameField.getText().toString();
            int age = Integer.parseInt(ageField.getText().toString());
            String sex = sexField.getSelectedItem().toString();
            Date scanCreationDate = submissionToEdit.getScanCreationDate();
            Date submissionCreationDate = submissionToEdit.getSubmissionCreationDate();
            SubmissionModel.Confirmation confirmation = predictionConfirmation;
            MLHelper.Prediction prediction = submissionToEdit.getPrediction();
            byte[] crxPhoto = submissionToEdit.getCxrPhoto();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            SubmissionModel.XrayType xrayType = SubmissionModel.XrayType.CXR;
            Timestamp learntAt = submissionToEdit.getLearntAt();

            return new SubmissionModel(xRayID, userId, firstName, lastName, age, sex, scanCreationDate, submissionCreationDate, confirmation, prediction, crxPhoto, xrayType, learntAt);
        } else {
            return null;
        }
    }

    /**
     * Validate fields, make sure no fields are empty or invalid if so prompt user
     * @return true for all fields are valid and false for at least one field invalid
     */
    private boolean validateFields(){
        boolean isFirstNameValid = !firstnameField.getText().toString().isEmpty();

        if(!isFirstNameValid){
            firstnameField.setError("Please enter first name");
        }


        boolean isLastNameValid = true;
        //Last Name is not a requirement
        //        boolean isLastNameValid = !lastnameField.getText().toString().isEmpty();
        //
        //        if(!isLastNameValid){
        //            lastnameField.setError("Please enter first name");
        //        }

        boolean isAgeValid = true;
        //Age is not a requirement
        //        boolean isAgeValid = !ageField.getText().toString().isEmpty();
        //
        //        if(!isAgeValid){
        //            ageField.setError("Please enter valid age");
        //        }

        if(isFirstNameValid && isLastNameValid && isAgeValid){
            return true;
        }
        return false;
    }


    /**
     * If the user confirms set the prediction confirmation prompt to appear and then if user agrees populate the prediction confirmed card.
     */
    private void setPredictionConfirmation(){
        //Checking that prediction is null, will also prevent user from causing issues by pressing buttons.
        if(prediction != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Submit");
            builder.setMessage("By confirming a diagnosis you agree that you have received an official diagnosis outside of this app and have been tested and diagnosed by a professional COVID-19 medical center.\n\nAre you sure you wish to confirm the prediction " + prediction.getFirst() + " as correct?")
                    .setCancelable(false)
                    .setPositiveButton(R.string.response_i_agree, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            confirmationRequestButtons.setVisibility(View.GONE);
                            predictionConfirmation = SubmissionModel.Confirmation.valueOf(prediction.getFirst().replaceAll("-", "").toUpperCase());
                            replacePredictionWithConfirmation();
                        }
                    })
                    .setNegativeButton(R.string.response_decline,new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog,int id)
                        {
                            dialog.cancel();
                        }
                    });
            AlertDialog  alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void setPredictionConfirmationWithDialog(){
        if(prediction != null){
            //Start by dismissing keyboard if its showing
            closeKeyboard();

            final AlertDialog.Builder agreementConfirmDiagnosisDialogBuilder = new AlertDialog.Builder(getContext());
            agreementConfirmDiagnosisDialogBuilder.setTitle("Prediction Confirmation")
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setMessage("By confirming a diagnosis you agree that you have received an official diagnosis outside of this app and have been tested and diagnosed by a professional COVID-19 medical center.\n\nPlease select the diagnosis you received from your doctor.")
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //If true, make user pick a classification.
                            final String[] classfications = new String[]{"COVID-19", "Pneumonia", "Normal"};
                            AlertDialog.Builder askForTypeDialogbuilder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
                            askForTypeDialogbuilder.setTitle("Prediction Confirmation");
                            askForTypeDialogbuilder.setIcon(R.drawable.ic_baseline_warning_24);
                            askForTypeDialogbuilder.setSingleChoiceItems(classfications, -1, null);
                            askForTypeDialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    int choice = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                    if (choice > -1 && choice < classfications.length) {
                                        String choiceClassification = classfications[choice];
                                        predictionConfirmation = SubmissionModel.Confirmation.valueOf(choiceClassification.replaceAll("-", "").toUpperCase());
                                        confirmationRequestButtons.setVisibility(View.GONE);
                                        replacePredictionWithConfirmation();
                                    } else {
                                        ToastHelper.showShortToast(getContext(), "Please select a choice.");
                                    }
                                }
                            });
                            AlertDialog  askForTypeAlertDialog = askForTypeDialogbuilder.create();
                            askForTypeAlertDialog.show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog  agreementConfirmationDialog = agreementConfirmDiagnosisDialogBuilder.create();
            agreementConfirmationDialog.show();

        }
    }

    private void ifContinualAITeachModel(String submissionId, SubmissionModel.Confirmation predictionConfirmation){
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        boolean allowCTScan = sharedpreferences.getBoolean("allowCTScan", false);
        boolean enableContinualAi = sharedpreferences.getBoolean("enableContinualAi", false);
        if(enableContinualAi){
            CloudMLXrayContinualServerClient.train(submissionId, predictionConfirmation.getDescription());
        }
    }

    private void replacePredictionWithConfirmation(){
        String confirmedPrediction = predictionConfirmation.getDescription();
        resultOutput.setText(confirmedPrediction);
        resultOutput.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        coloriseOutput(resultOutput, confirmedPrediction);
        resultDetails.setText("CONFIRMED");
    }

    // Colorise Output
    private void coloriseOutput(TextView textView, String result) {
        //Make it red if COVID, blue if Normal and Orange if Pneumonia
        if (result.contains(getString(R.string.classification_covid_19))) {
            textView.setTextColor(Color.RED);
        } else if (result.contains(getString(R.string.classification_pneumonia))) {
            textView.setTextColor(Color.rgb(255, 165, 0));
        } else if (result.contains(getString(R.string.classification_normal))) {
            textView.setTextColor(getResources().getColor(R.color.colorPrimaryLight, getActivity().getTheme()));
        }
    }

    /**
     * Close the keyboard by using the KeyboardHelper class
     */
    private void closeKeyboard(){
        View view = getDialog().getCurrentFocus();
        KeyboardHelper.hideSoftKeyboard(view, getActivity());
    }
}
