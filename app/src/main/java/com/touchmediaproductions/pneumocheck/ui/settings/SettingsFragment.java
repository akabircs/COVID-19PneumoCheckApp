package com.touchmediaproductions.pneumocheck.ui.settings;

import static com.touchmediaproductions.pneumocheck.survey.SurveyActivity.SURVEY_COMPLETED;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.CloudMLXrayContinualServerClient;
import com.touchmediaproductions.pneumocheck.helpers.DataBaseHelper;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.FolderHelper;
import com.touchmediaproductions.pneumocheck.helpers.PermissionsHelper;
import com.touchmediaproductions.pneumocheck.helpers.ResearchTests;
import com.touchmediaproductions.pneumocheck.helpers.TestImageSetInferenceHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.models.UserProfile;
import com.touchmediaproductions.pneumocheck.survey.SurveyActivity;
import com.touchmediaproductions.pneumocheck.survey.SurveyResults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final int REQUEST_ATTEMPT_SURVEY = 101;

    // Must be 10+
    private final int BATCH_TEST_LOCAL_LIMIT = 200;
    private final int BATCH_TEST_REMOTE_LIMIT = 100;

    private ListenerRegistration mListenerRegistration;

    //Firebase Auth
    private FirebaseAuth fireBaseAuth;

    //Buttons
    private Button logoutButton;
    private Button completeSurveyButton;
    private Button clearCache;
    private Button btnChooseModel;
    private Button deleteDatabase;
    private Button deleteAccount;
    private Button addDoctor;

    //Toggle Switch
    private SwitchMaterial enableCTScanSwitch;
    private SwitchMaterial enableContinualAISwitch;

    //Developer
    private TextView developerRunLocalTestDescriptionTextView;
    private Button developerButtonRunLocalTests;
    private TextView developerOutputRunLocalTests;
    private TextView developerRunCloudTestDescriptionTextView;
    private Button developerButtonRunCloudTests;
    private TextView developerOutputRunCloudTests;

    //Display Text
    private TextView displayNameTextView;
    private TextView userNameTextView;
    private TextView accountTypeTextView;
    private TextView ageTextView;
    private TextView sexTextView;

    private LinearLayout ageLinearLayout;
    private LinearLayout sexLinearLayout;

    //Pane Title
    private TextView pageTitle;

    //Profile Survey Complete Card
    private MaterialCardView completeSurveyCard;

    //Associated Accounts
    private TextView associatedAccountsLabel;

    //Override Model Local Choose
    private MaterialCardView overrideModelCard;

    //Survey State
    private FirestoreRepository.SurveyState surveyState;

    //Authenticated User
    private String authenticatedUserUID;

    //Connect the ViewModel
    private SettingsViewModel settingsViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Connect the ViewModel
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        //Inflate the Fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        pageTitle = root.findViewById(R.id.text_settings);
        pageTitle.setText("SETTINGS");

        int dp8 = (int) DimensionHelper.dpTopixel(getContext(), 8);
        int dp35 = (int) DimensionHelper.dpTopixel(getContext(), 35);
        Drawable drawable = getResources().getDrawable(R.drawable.logoicon, getContext().getTheme());
        drawable.setBounds(0, 0, dp35, dp35);

        pageTitle.setCompoundDrawables(drawable, null, null, null);
        pageTitle.setCompoundDrawablePadding(dp8);

        //SURVEY
        completeSurveyCard = root.findViewById(R.id.cardview_settings_surveycompletioncard);
        completeSurveyButton = root.findViewById(R.id.button_settings_completesurvey);
        completeSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSurvey = new Intent(getActivity(), SurveyActivity.class);
                startActivityForResult(startSurvey, REQUEST_ATTEMPT_SURVEY);
            }
        });

        //PROFILE CARD
        displayNameTextView = root.findViewById(R.id.textview_settings_firstname);
        userNameTextView = root.findViewById(R.id.textview_settings_username);
        accountTypeTextView = root.findViewById(R.id.textview_settings_accounttype);
        ageTextView = root.findViewById(R.id.textview_settings_age);
        sexTextView = root.findViewById(R.id.textview_settings_sex);

        ageLinearLayout = root.findViewById(R.id.linearlayout_settings_age);
        sexLinearLayout = root.findViewById(R.id.linearlayout_settings_sex);

        addDoctor = root.findViewById(R.id.button_settings_adddoctor);
        // On click show dialog to enter code
        addDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDoctorDialog();
            }
        });

        //Delete Database
        deleteDatabase = root.findViewById(R.id.button_settings_cleardb);
        deleteDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDatabaseWithAlert();
            }
        });

        //DELETE ACCOUNT
        deleteAccount = root.findViewById(R.id.button_settings_deleteaccount);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener deleteAccountDialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //DELETE ACCOUNT!!!!
                                commitDeleteUserAccount();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do nothing
                                break;
                        }
                    }
                };
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
                builder.setTitle("Delete Account?").setIcon(R.drawable.ic_baseline_warning_24).setMessage("Are you sure you want to delete your account and associated profile? (This cannot be undone)").setPositiveButton("Yes", deleteAccountDialogClickListener)
                        .setNegativeButton("No", deleteAccountDialogClickListener).show();

            }
        });

        //CACHE
        clearCache = root.findViewById(R.id.button_settings_clearcache);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (askStoragePermission()) {
                    int filesLeft = FolderHelper.clearPicturesFolder(requireActivity());
                    ToastHelper.showShortToast(getContext(), "Cleared " + filesLeft + " file(s).");
                }
            }
        });

        //Override Model
        overrideModelCard = root.findViewById(R.id.materialcard_settings_overridemodellocalchoose);
        btnChooseModel = root.findViewById(R.id.button_settings_choosemodel);
        btnChooseModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Choose model Here
            }
        });

        //User Related Details UI
        logoutButton = root.findViewById(R.id.button_settings_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutOfUserAccount();
            }
        });
        //Load Firebase
        fireBaseAuth = fireBaseAuth.getInstance();
        authenticatedUserUID = fireBaseAuth.getCurrentUser().getUid();

        //Show Currently logged in user in user info card.
        displayCurrentAuthenticatedUser();

        //Associated Acocunts
        associatedAccountsLabel = root.findViewById(R.id.textview_settings_associatedaccountslabel);

        SharedPreferences sharedpreferences = requireActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);

        //Enable CTScan Functionality
        enableCTScanSwitch = root.findViewById(R.id.materialswitch_settings_allowctscan);
        enableCTScanSwitch.setChecked(sharedpreferences.getBoolean("allowCTScan", false));
        enableCTScanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Enable CT Scan
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("allowCTScan", b);
                editor.apply();
                Log.i(TAG, "CTScan Switch:" + b);
            }
        });

        // Local or Cloud Continual AI Switch
        enableContinualAISwitch = root.findViewById(R.id.materialswitch_settings_enablecontinualai);
        enableContinualAISwitch.setChecked(sharedpreferences.getBoolean("enableContinualAi", false));
        enableContinualAISwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // Save to shared preferences for the app
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("enableContinualAi", b);
                editor.apply();
                Log.i(TAG, "Continual AI Switch:" + b);
            }
        });

        //DEVELOPER TOOL
        MaterialCardView developerCard = root.findViewById(R.id.cardview_settings_developercard);
        developerCard.setVisibility(View.VISIBLE);

        developerRunLocalTestDescriptionTextView = root.findViewById(R.id.textview_settings_developer_runlocaltestdescription);
        developerOutputRunLocalTests = root.findViewById(R.id.textview_settings_run_local_tests_debugoutput);
        developerButtonRunLocalTests = root.findViewById(R.id.button_settings_run_local_tests);
        developerButtonRunLocalTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                developerOutputRunLocalTests.setText("Starting Local Test...");
                developerButtonRunLocalTests.setVisibility(View.GONE);
                // Play a notification sound
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(requireActivity().getApplicationContext(), notification);
                r.play();
                new Thread(() -> {
                    final int localLimit = BATCH_TEST_LOCAL_LIMIT;
                    TestImageSetInferenceHelper.ImageSet[] imageSets = TestImageSetInferenceHelper.getImageUrls(requireActivity(), localLimit);
                    Log.i(TAG, "ImageSets:" + imageSets.length);
                    ResearchTests.LoadTimeTestResults loadingTimeResults = ResearchTests.runLoadingTest(requireActivity(), "covidxray_densenet161.ptl", localLimit);
                    ResearchTests.InferenceTimeTestResults inferenceTimeResults = ResearchTests.runInferenceTest(requireActivity(), "covidxray_densenet161.ptl", imageSets);
                    String sb = loadingTimeResults.getLoadTimeTestsAsCSV() + "\n\n" + loadingTimeResults.getLoadTimeResultsAsCSV() + "\n\n\n" +
                            inferenceTimeResults.getInferenceTimeTestsAsCSV() + "\n\n" + inferenceTimeResults.getInferenceTimeResultsAsCSV();
                    FirestoreRepository.pushLocalTestResults(requireActivity(),loadingTimeResults, inferenceTimeResults);
                    developerOutputRunLocalTests.post(() -> {
                        developerOutputRunLocalTests.setText(sb);
                        developerButtonRunLocalTests.setVisibility(View.VISIBLE);
                    });
                    // Play a notification sound
                    requireActivity().runOnUiThread(() -> {
                        try {
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }).start();
            }
        });

        developerRunCloudTestDescriptionTextView = root.findViewById(R.id.textview_settings_developer_runcloudtestdescription);
        developerOutputRunCloudTests = root.findViewById(R.id.textview_settings_run_cloud_tests_debugoutput);
        developerButtonRunCloudTests = root.findViewById(R.id.button_settings_run_cloud_tests);
        // Hide Cloud tests as they are done on the cloud instead
        developerRunCloudTestDescriptionTextView.setVisibility(View.GONE);
        developerOutputRunCloudTests.setVisibility(View.GONE);
        developerButtonRunCloudTests.setVisibility(View.GONE);

        developerButtonRunCloudTests.setOnClickListener(view -> {
            developerOutputRunCloudTests.setText("Starting request...");
            developerButtonRunCloudTests.setVisibility(View.GONE);
            new Thread(() -> {

                final int localLimit = BATCH_TEST_REMOTE_LIMIT;

                // Time the request
                long startLocalTime = System.currentTimeMillis();

                // Trigger a custom function here:
                ///////////////////////////
                TestImageSetInferenceHelper.ImageSet[] imageSets = TestImageSetInferenceHelper.getImageUrls(requireActivity(), localLimit);

                Log.i(TAG, "ImageSets:" + imageSets.length + " " + Arrays.toString(imageSets));

                // Generate random string for the request
                String testBatchSubmissionId = UUID.randomUUID().toString();

                for (int i = 0; i < localLimit; i++) {
                    TestImageSetInferenceHelper.ImageSet imageSet = imageSets[i];
                    // Queue Flips the order so last is first
                    boolean isLast = (i == 0);
                    CloudMLXrayContinualServerClient.classifySpecificImageUrl(testBatchSubmissionId, imageSet.diagnosis, imageSet.imageUrl, isLast);
                }

                // Check whether firebase has a predictions array length of localLimit in the document with submissionId

                DocumentReference documentSnapshot = FirestoreRepository.getTestBatchResults(testBatchSubmissionId);
                mListenerRegistration = documentSnapshot.addSnapshotListener((documentSnapshot1, e) -> {
                    if (documentSnapshot1 != null && documentSnapshot1.exists()) {

                        Timestamp startedAtServerTime = (Timestamp) documentSnapshot1.get("startedAt");
                        Timestamp endedAtServerTime = (Timestamp) documentSnapshot1.get("endedAt");

                        Log.i(TAG, "DocumentSnapshot: " + startedAtServerTime + " " + endedAtServerTime);

                        Map<String, Object> data = documentSnapshot1.getData();
                        List<Map<String, Object>> predictions = null;
                        if (data != null) {
                            predictions = (List<Map<String, Object>>) data.get("predictions");
                        }

                        if (startedAtServerTime != null && predictions != null && predictions.size() == localLimit) {
                            // Calculate accuracy based on the imageUrl if imageSet[i].imageUrl == prediction[i].imageUrl check if prediction[i].diagnosis == imageSet[i].diagnosis
                            int correct = 0;
                            for (int i = 0; i < localLimit; i++) {
                                Map<String, Object> prediction = predictions.get(i);
                                String imageUrl = (String) prediction.get("imageUrl");
                                String predictionDiagnosis = (String) prediction.get("prediction");
                                for (TestImageSetInferenceHelper.ImageSet imageSet : imageSets) {
                                    if (imageSet.imageUrl.equals(imageUrl)) {
                                        if (imageSet.diagnosis.equals(predictionDiagnosis)) {
                                            correct++;
                                        }
                                    }
                                }
                            }

                            float accuracy = ((float) correct / (float) localLimit) * 100;

                            developerOutputRunCloudTests.post(() -> {
                                // Calculate time taken
                                long endLocalTime = System.currentTimeMillis();
                                long timeTaken = endLocalTime - startLocalTime; // milliseconds
                                float timeTakenSeconds = (float) timeTaken / 1000; // seconds
                                // Calculate per inference
                                long timePerInference = timeTaken / localLimit; // milliseconds
                                float timePerInferenceSeconds = (float) timePerInference / 1000; // seconds

                                // Try calculate but if endTime is null then it will be null
                                float serverTimePerInferenceSeconds = 0;
                                float serverTimeTakenSeconds = 0;
                                if (endedAtServerTime != null) {
                                    serverTimeTakenSeconds = (endedAtServerTime.getSeconds() - startedAtServerTime.getSeconds());
                                    serverTimePerInferenceSeconds = (float) (endedAtServerTime.getSeconds() - startedAtServerTime.getSeconds()) / localLimit;
                                }

                                String sb = "Finished request inference on " + localLimit + " images\nLocal Time taken: " +
                                        timeTaken + "ms (" + timeTakenSeconds + "s)\nLocal Time per inference: " +
                                        timePerInference + "ms (" + timePerInferenceSeconds + "s)\n" + (endedAtServerTime != null ? "Server Time Taken: " +
                                        serverTimeTakenSeconds +
                                        "s\nTime per inference: " + serverTimePerInferenceSeconds + "s" : "Server Time not available") +
                                        "\nAccuracy " + accuracy + "%";

                                developerOutputRunCloudTests.setText(sb);
                                developerButtonRunCloudTests.setVisibility(View.VISIBLE);
                                mListenerRegistration.remove();
                            });
                        }
                    }
                });
            }).start();
        });
        ///////////////////////////


        settingsViewModel.getUserProfile(authenticatedUserUID).

                observe(requireActivity(), new Observer<UserProfile>() {
                    @Override
                    public void onChanged(UserProfile userProfiles) {
                        FirestoreRepository.AccountType accountType = FirestoreRepository.AccountType.valueOf(userProfiles.getAccountType().toLowerCase());
                        accountTypeTextView.setText(accountType.toString().toUpperCase());
                        //Hide Unnecessary Fields as a Participant:
                        if (accountType == FirestoreRepository.AccountType.participant) {
                            updateUIToMatchParticipantView();
                        } else if (accountType == FirestoreRepository.AccountType.doctor) {
                            updateUIToMatchDoctorView();
                        } else if (accountType == FirestoreRepository.AccountType.researcher) {
                            updateUIToMatchResearcherView();
                        }
                        surveyState = FirestoreRepository.SurveyState.valueOf(userProfiles.getSurveyState());
                        if (surveyState != null && surveyState == FirestoreRepository.SurveyState.complete) {
                            sexTextView.setText(userProfiles.getSex().toUpperCase());
                            ageTextView.setText(userProfiles.getAge().toUpperCase());
                            sexLinearLayout.setVisibility(View.VISIBLE);
                            ageLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            sexLinearLayout.setVisibility(View.GONE);
                            ageLinearLayout.setVisibility(View.GONE);
                        }
                    }
                });

        return root;
    }

    private void showAddDoctorDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(SettingsFragment.this.getContext());
        alert.setTitle("Pair With Doctors");
        alert.setMessage("Enter code to pair with doctor");
        // Set an EditText view to get user input
        final EditText input = new EditText(SettingsFragment.this.getContext());
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = String.valueOf(input.getText());
                if (value.equals("")) {
                    Toast.makeText(SettingsFragment.this.getContext(), "Please enter a valid code", Toast.LENGTH_SHORT).show();
                } else {
                    // Attempt to add doctor to the list of doctors, if code is valid adds the doctor to the list of doctors
                    //if code is not valid, reject code
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void updateUIToMatchResearcherView() {
        associatedAccountsLabel.setText("Associated Accounts");
        overrideModelCard.setVisibility(View.VISIBLE);
    }

    private void updateUIToMatchParticipantView() {
        associatedAccountsLabel.setText("My Doctors");
        overrideModelCard.setVisibility(View.GONE);
    }

    private void updateUIToMatchDoctorView() {
        associatedAccountsLabel.setText("My Patients");
        completeSurveyCard.setVisibility(View.GONE);
    }

    /**
     * Logout of User Account and return to previous activity.
     */
    private void logoutOfUserAccount() {
        logoutButton.setEnabled(false);
        if (fireBaseAuth.getCurrentUser() != null) {
            fireBaseAuth.signOut();
            if (fireBaseAuth.getCurrentUser() == null) {
                userNameTextView.setText("");
                displayNameTextView.setText("");
                logoutButton.setText("Logged out");


                //Clear out the ViewModels
                getActivity().getViewModelStore().clear();
                //Close Activity
                getActivity().finish();
            }
        }
    }

    private void displayCurrentAuthenticatedUser() {
        FirebaseUser firebaseUser = fireBaseAuth.getCurrentUser();
        String displayName = firebaseUser.getDisplayName();
        String userName = firebaseUser.getEmail();
        //If displayName is empty then make it show the email
        if (displayName == null || displayName.isEmpty()) {
            displayName = userName;
        }
        displayNameTextView.setText(displayName);
        userNameTextView.setText(userName);

    }

    /**
     * Erase the database with prompt
     */
    private void deleteDatabaseWithAlert() {
        if (askStoragePermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("WARNING!");
            builder.setMessage("Are you sure you want to erase the entire PneumoCheck submissions database? This cannot be undone.")
                    .setCancelable(false)
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().deleteDatabase(DataBaseHelper.DATABASE_FILENAME);
                            ToastHelper.showLongToast(getContext(), "Database Erased");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Ask for runtime storage permission (READ or WRITE)
     *
     * @return
     */
    private boolean askStoragePermission() {
        return new PermissionsHelper(getActivity()).checkStoragePermission();
    }

    /**
     * Delete user account login and user profile from Cloud Firebase
     */
    private void commitDeleteUserAccount() {
        FirestoreRepository.deleteUserProfile(fireBaseAuth.getCurrentUser().getUid());
        fireBaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Your user account has been deleted.");
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ATTEMPT_SURVEY:
                if (resultCode == SURVEY_COMPLETED) {
                    if (data != null) {
                        SurveyResults surveyResults = data.getExtras().getParcelable("SurveyResults");
                        //Survey Returned - update profile:
                        saveSurveyToDatabase(authenticatedUserUID, surveyResults);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void saveSurveyToDatabase(String userUID, SurveyResults surveyResults) {
        FirestoreRepository.saveSurveyResults(userUID, surveyResults);
    }
}