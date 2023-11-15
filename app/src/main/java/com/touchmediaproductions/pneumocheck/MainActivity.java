package com.touchmediaproductions.pneumocheck;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.touchmediaproductions.pneumocheck.helpers.CropImageHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.PermissionsHelper;
import com.touchmediaproductions.pneumocheck.helpers.PictureHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.helpers.XRayNotXRayHelper;
import com.touchmediaproductions.pneumocheck.models.UserProfile;
import com.touchmediaproductions.pneumocheck.ui.EditFormActivity;
import com.touchmediaproductions.pneumocheck.ui.settings.SettingsViewModel;
import com.touchmediaproductions.pneumocheck.ui.submissions.SubmissionsViewModel;
import com.yalantis.ucrop.UCrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main Starting Activity
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_REFRESH_SUBMISSIONS_FRAGMENT = 233;
    private static final String TAG = "MainActivity";

    //Utility Classes used
    private PermissionsHelper permissionsHelper;
    private PictureHelper pictureHelper;

    //The X-Ray image
    private PictureHelper.Picture picture;

    //Add button
    private FloatingActionButton addSubmissionButton;
    //'Add From' Popup
    private FrameLayout addPhotoHolder;
    private LinearLayout addFromCardView;
    private Button addFromCamera;
    private Button addFromDeviceGallery;
    private ImageView galleryIcon;
    private ImageView cameraIcon;

    //Navigation
    private AppBarConfiguration appBarConfiguration;
    private NavController navigationController;
    private BottomNavigationView bottomNavigationView;

    //User Details
    private String userId;
    private FirestoreRepository.AccountType accountType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_app_bar);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Access the Settings viewmodel
        SettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        //Connect the ViewModel
        SubmissionsViewModel submissionsViewModel = new ViewModelProvider(this).get(SubmissionsViewModel.class);

        settingsViewModel.getUserProfile(userId).observe(this, new Observer<UserProfile>() {
            @Override
            public void onChanged(UserProfile userProfiles) {
                accountType = FirestoreRepository.AccountType.valueOf(userProfiles.getAccountType().toLowerCase());
 /*               surveyState = FirestoreRepository.SurveyState.valueOf(userProfiles.getSurveyState());
                if (accountType == FirestoreRepository.AccountType.participant && surveyState == FirestoreRepository.SurveyState.neverattempted) {
                    //If never attempted show dialog box and once dismissed transfer user to the

                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setBackground(MainActivity.this.getDrawable(R.drawable.dialog_background))
                            .setTitle("Finish Setting up your Profile")
                            .setMessage("Please complete profile survey.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    navigationController.navigate(R.id.navigation_settings);
                                }
                            })
                            .setIcon(R.drawable.ic_baseline_person_outline_24)
                            .show();
                }*/
            }
        });

        //Reset picture
        picture = null;

        //Setup activity helpers
        permissionsHelper = new PermissionsHelper(this);
        pictureHelper = new PictureHelper(this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_info, R.id.navigation_submissions, R.id.navigation_settings, R.id.navigation_help)
                .build();
        navigationController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navigationController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navigationController);

        //Hide the bar, no one wants to see it.
        getSupportActionBar().hide();

        //Floating Add Button
        addSubmissionButton = findViewById(R.id.button_mainactivity_addsubmission);


        addFromCardView = findViewById(R.id.linearlayout_mainactivity_addphotochooser);

        //Popup
        addPhotoHolder = findViewById(R.id.framelayout_mainactivity_addphotochooserholder);
        //Make frame transparent
        addPhotoHolder.getBackground().setAlpha(0);
        addPhotoHolder.setClickable(false);
        addFromCamera = findViewById(R.id.button_addsubmission_add_from_camera);
        addFromDeviceGallery = findViewById(R.id.button_addsubmission_add_from_device_gallery);
        cameraIcon = findViewById(R.id.imageview_addsubmission_cameraicon);
        galleryIcon = findViewById(R.id.imageview_addsubmission_galleryicon);

        addSubmissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPopUpCardShowing()) {
                    hideAddPhotoMenu();
                } else {
                    showAddPhotoMenu();
                }
            }
        });

        addFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionsHelper.checkMultiplePermissions(Manifest.permission.CAMERA)) {
                    picture = null;
                    pictureHelper.launchTakePictureIntent();
                }
            }
        });

        addFromDeviceGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picture = null;
                pictureHelper.selectPhoto();
            }
        });

        refreshSubmissionsFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAnyCompressedFilesFromCache();
    }

    private void clearAnyCompressedFilesFromCache() {
        File cacheDirectory = this.getCacheDir();
        if (cacheDirectory != null && cacheDirectory.isDirectory()) {
            for (File file : cacheDirectory.listFiles()) {
                if (file.getName().endsWith(".zip")) {
                    file.delete();
                }
            }
        }
        Log.i("CACHE", "Cleared Compressed Files");
    }

    /**
     * Show the add photo menu
     */
    private void showAddPhotoMenu() {
        animatePopUpCardViewAppear();
        transformFABIntoCloseButton();
    }

    /**
     * Hide the add photo menu
     */
    private void hideAddPhotoMenu() {
        animatePopUpCardViewDisappear();
        transformFABIntoAddButton();
    }

    ;

    private void transformFABIntoAddButton() {
        ViewPropertyAnimator animator = addSubmissionButton.animate();
        animator.rotation(0).setDuration(500).setInterpolator(new AccelerateInterpolator());
        addSubmissionButton.clearColorFilter();
    }

    private void transformFABIntoCloseButton() {
        ViewPropertyAnimator animator = addSubmissionButton.animate();
        animator.rotation(135).setDuration(500).setInterpolator(new AccelerateInterpolator());
        addSubmissionButton.setColorFilter(Color.RED);
    }

    private boolean isPopUpCardShowing() {
        return addFromCardView.getVisibility() == View.VISIBLE;
    }

    private void animatePopUpCardViewAppear() {
        //Make add submission menu appear
        addFromCardView.animate().translationX(0).setDuration(500).setInterpolator(new AccelerateInterpolator()).withStartAction(new Runnable() {
            @Override
            public void run() {
                addFromCardView.setVisibility(View.VISIBLE);
            }
        });
        addPhotoHolder.getBackground().setAlpha(255);
        addPhotoHolder.setClickable(true);
    }

    private void animatePopUpCardViewDisappear() {
        //Make add submission menu disappear
        addFromCardView.animate().translationX(-400).setDuration(200).setInterpolator(new AccelerateInterpolator()).alpha(0.2f).setDuration(200).withStartAction(new Runnable() {
            @Override
            public void run() {
                addFromCardView.setVisibility(View.GONE);
            }
        });
        addPhotoHolder.getBackground().setAlpha(0);
        addPhotoHolder.setClickable(false);
    }

    //Required for handling permission prompt results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean successfulResponse = permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!successfulResponse) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //A flag used to indicate where the picture came from/ how it was uploaded in order to process accordingly
        boolean isFromGallery = false;

        switch (requestCode) {
            case PictureHelper.REQUEST_PHOTO_GALLERY:
                isFromGallery = true;
            case PictureHelper.REQUEST_CAPTURE_FULLSIZE_PHOTO:
            case PictureHelper.REQUEST_CAPTURE_THUMBNAIL_PHOTO:
                picture = pictureHelper.onActivityResultForPicture(requestCode, resultCode, data);
                //If picture is valid and loaded
                if (picture != null && picture.getImgUri() != null) {
                    if (picture.getPictureBitmap() != null) {
                        //Check whether image is a valid chest X-Ray and not a random image.
                        if (XRayNotXRayHelper.isValidChestXRay(MainActivity.this, picture.getPictureBitmap())) {
                            //No Images smaller than 224 x 224
                            if (picture.getPictureBitmap().getWidth() >= 224 && picture.getPictureBitmap().getHeight() >= 224) {
                                picture.setIsGallery(isFromGallery);
                                //Picture is ready to be used.
                                try {
                                    String mimeType = getMimeType(picture.getImgUri());
                                    if (mimeType.matches(".*jpg.*|.*jpeg.*|.*png.*")) {
                                        CropImageHelper.startUCropActivity(MainActivity.this, picture.getImgUri());
                                    } else {
                                        ToastHelper.showLongToast(MainActivity.this, "That image was of type " + mimeType + ". Please select only either PNG, JPEG or JPG.");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    ToastHelper.showShortToast(MainActivity.this, "Couldn't save cropped image.");
                                }
                            } else {
                                ToastHelper.showShortToast(MainActivity.this, "Please provide an image that is at least 224 by 224 pixels large.");
                            }
                        } else {
                            ToastHelper.showLongToast(MainActivity.this, "The image provided was not a valid Chest X-Ray.");
                        }

                    }
                }
                break;
            case UCrop.REQUEST_CROP:
                if (data != null) {
                    //If the data returned is valid then run it through UCrop and start the activity for cropping
                    final Uri resultUriUCrop = UCrop.getOutput(data);
                    picture.setCroppedImage(resultUriUCrop);
                    launchEditSubmissionActivity(picture, userId, accountType);
                }
                break;
            case EditFormActivity.CREATE_SUBMISSION_REQUEST_CODE:
                if (resultCode == MainActivity.REQUEST_REFRESH_SUBMISSIONS_FRAGMENT) {
                    //Hide the pop up add photo menu
                    hideAddPhotoMenu();
                    //Refresh the submissions page to show new additions
                    refreshSubmissionsFragment();
                }
                break;
            default:
                break;
        }
    }

    private void refreshSubmissionsFragment() {
        //Navigate to the Submissions fragment, forcing it to refresh as it calls its onCreate()
        navigationController.navigate(R.id.navigation_submissions);
    }

    /**
     * Start the Edit Form submission activity
     *
     * @param picture
     */
    private void launchEditSubmissionActivity(PictureHelper.Picture picture, String userId, FirestoreRepository.AccountType accountType) {
        Intent editFormSubmissionIntent = new Intent(this, EditFormActivity.class);
        editFormSubmissionIntent.putExtra("userId", userId);
        editFormSubmissionIntent.putExtra("accountType", accountType);
        editFormSubmissionIntent.putExtra("croppedUri", picture.getCroppedImageUri());
        editFormSubmissionIntent.putExtra("fullPictureUri", picture.getImgUri());
        editFormSubmissionIntent.putExtra("isGalleryPhoto", picture.isGallery());
        startActivityForResult(editFormSubmissionIntent, EditFormActivity.CREATE_SUBMISSION_REQUEST_CODE);
    }

    /**
     * Used to check the type of the image picked
     * For Example: this application does not support WEBP as a result, a check must be performed
     *
     * @param uri
     * @return
     */
    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //Use the ContentResolver to get the type of the image.
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            //If the content resolver could not be used, instead use the file extension to figure it out.
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    /**
     * Get System Info
     */
    public static String getSystemInfoString(Context context){
        String systemInfo = "";
        try {
            systemInfo += "Product: " + Build.PRODUCT + "\n";
            systemInfo += "Device: " + Build.DEVICE + "\n";
            systemInfo += "Model: " + Build.MODEL + "\n";
            systemInfo += "Brand: " + Build.BRAND + "\n";
            systemInfo += "Manufacturer: " + Build.MANUFACTURER + "\n";
            systemInfo += "Android Version: " + Build.VERSION.RELEASE + "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // Available RAM
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            // Available internal memory
            systemInfo += "Available Memory: " + ((float) mi.availMem / 1073741824L) + "GB\n";
            // Total RAM
            systemInfo += "Total Memory: " + ((float) mi.totalMem / 1073741824L) + "GB\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        // CPU Ghz and cores
        try {
            FileReader fileReader = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            String[] split = line.split(" ");
            String cpuHz = split[0];
            // Convert to Ghz
            double cpuGhz = Double.parseDouble(cpuHz) / 1000000;
            systemInfo += "CPU Ghz: " + cpuGhz + "GHz\n";
            systemInfo += "CPU Cores: " + Runtime.getRuntime().availableProcessors() + "\n";
            // CPU Brand
            FileReader fileReaderBrand = new FileReader("/proc/cpuinfo");
            BufferedReader bufferedReaderBrand = new BufferedReader(fileReaderBrand);
            String lineBrand = bufferedReaderBrand.readLine();
            String[] splitBrand = lineBrand.split(":");
            String cpuBrand = splitBrand[1];
            systemInfo += "CPU Brand: " + cpuBrand + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return systemInfo;
    }
}