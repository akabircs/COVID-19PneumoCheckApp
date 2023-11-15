package com.touchmediaproductions.pneumocheck.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.FolderHelper;
import com.touchmediaproductions.pneumocheck.helpers.PermissionsHelper;
import com.touchmediaproductions.pneumocheck.helpers.TestImageSetInferenceHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.models.UserProfile;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    // Must be 10+
    private final int BATCH_TEST_LOCAL_LIMIT = 200;
    private final int BATCH_TEST_REMOTE_LIMIT = 100;

    private ListenerRegistration mListenerRegistration;

    //Firebase Auth
    private FirebaseAuth fireBaseAuth;

    //Buttons
    private Button logoutButton;

    private Button clearCache;
    private Button btnChooseModel;
    private Button deleteDatabase;
    private Button deleteAccount;
    private Button addDoctor;

    //Toggle Switch
    private SwitchMaterial enableCTScanSwitch;
    private SwitchMaterial enableContinualAISwitch;

    private MaterialRadioButton radioLocal;
    private MaterialRadioButton radioCloud;

    // Cloud Server URL
    private EditText continualAIServerHostname;

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

    //Associated Accounts
    private TextView associatedAccountsLabel;

    //Override Model Local Choose
    private MaterialCardView overrideModelCard;

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

        //PROFILE CARD
        displayNameTextView = root.findViewById(R.id.textview_settings_firstname);
        userNameTextView = root.findViewById(R.id.textview_settings_username);
        accountTypeTextView = root.findViewById(R.id.textview_settings_accounttype);
        ageTextView = root.findViewById(R.id.textview_settings_age);
        sexTextView = root.findViewById(R.id.textview_settings_sex);

        ageLinearLayout = root.findViewById(R.id.linearlayout_settings_age);
        sexLinearLayout = root.findViewById(R.id.linearlayout_settings_sex);

//        addDoctor = root.findViewById(R.id.button_settings_adddoctor);
        // On click show dialog to enter code
//        addDoctor.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showAddDoctorDialog();
//            }
//        });

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
//        associatedAccountsLabel = root.findViewById(R.id.textview_settings_associatedaccountslabel);

        SharedPreferences sharedpreferences = requireActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
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

                        sexTextView.setText(userProfiles.getSex() != null ? userProfiles.getSex().toUpperCase() : "");
                        ageTextView.setText(userProfiles.getAge() != null ? userProfiles.getAge().toUpperCase() : "");
                        sexLinearLayout.setVisibility(View.VISIBLE);
                        ageLinearLayout.setVisibility(View.VISIBLE);
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
//        associatedAccountsLabel.setText("Associated Accounts");
        overrideModelCard.setVisibility(View.VISIBLE);
    }

    private void updateUIToMatchParticipantView() {
//        associatedAccountsLabel.setText("My Doctors");
        overrideModelCard.setVisibility(View.GONE);
    }

    private void updateUIToMatchDoctorView() {
//        associatedAccountsLabel.setText("My Patients");
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
}