package com.touchmediaproductions.pneumocheck.helpers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Takes care of Camera and Gallery related processing
 */
public class PictureHelper {

    public class Picture {
        private Bitmap pictureBitmap = null;
        private String photoPath = null;
        private Uri imgUri = null;
        private Uri croppedImage = null;
        private boolean isGallery;

        public Picture() {
        }

        ;

        public Picture(Bitmap pictureBitmap, String photoPath, Uri imgUri) {
            this.pictureBitmap = pictureBitmap;
            this.photoPath = photoPath;
            this.imgUri = imgUri;
        }

        public String getPhotoPath() {
            return photoPath;
        }

        public Bitmap getPictureBitmap() {
            return pictureBitmap;
        }

        public Uri getImgUri() {
            return imgUri;
        }

        public Uri getCroppedImageUri() {
            return croppedImage;
        }

        public void setPhotoPath(String photoPath) {
            this.photoPath = photoPath;
        }

        public void setPictureBitmap(Bitmap pictureBitmap) {
            this.pictureBitmap = pictureBitmap;
        }

        public void setImgUri(Uri imgUri) {
            this.imgUri = imgUri;
        }

        public void setCroppedImage(Uri croppedImage) {
            this.croppedImage = croppedImage;
        }

        public void setIsGallery(boolean b) {
            this.isGallery = b;
        }

        public boolean isGallery() {
            return this.isGallery;
        }
    }

    private final Activity activityContext;
    private Picture currentPicture;

    public static final int REQUEST_CAPTURE_THUMBNAIL_PHOTO = 1;
    public static final int REQUEST_CAPTURE_FULLSIZE_PHOTO = 2;
    public static final int REQUEST_PHOTO_GALLERY = 3;

    private static final String COM_TOUCHMEDIAPRODUCTIONS_ANDROID_FILEPROVIDER = "com.touchmediaproductions.android.fileprovider";

    public PictureHelper(Activity activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Launch photo gallery for user to choose image.
     */
    public void selectPhoto() {
        currentPicture = new Picture();
        Intent selectPhotoIntent = new Intent(Intent.ACTION_PICK);
        selectPhotoIntent.setType("image/*");
        try {
            activityContext.startActivityForResult(selectPhotoIntent, REQUEST_PHOTO_GALLERY);
        } catch (Exception ex) {
            ToastHelper.showShortToast(activityContext, "Gallery app is not available");
        }
    }

    /**
     * Launch Camera Intent for fetching a full size image.
     */
    public void launchTakePictureIntent() {
        currentPicture = new Picture();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = null;
            try {
                photoFile = createImageFile(activityContext, currentPicture);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ToastHelper.showShortToast(activityContext, "Error occurred while creating file: " + ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activityContext,
                        COM_TOUCHMEDIAPRODUCTIONS_ANDROID_FILEPROVIDER,
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                //Load photoURI onto the picture object
                currentPicture.setImgUri(Uri.fromFile(photoFile));

                activityContext.startActivityForResult(takePictureIntent, REQUEST_CAPTURE_FULLSIZE_PHOTO);
            }
        } catch (Exception ex) {
            ToastHelper.showShortToast(activityContext, "Camera app is not available");
        }
    }

    /**
     * Launch Camera Intent for fetching a thumbnail.
     */
    public void launchTakePictureIntentThumbnail() {
        currentPicture = new Picture();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            activityContext.startActivityForResult(takePictureIntent, REQUEST_CAPTURE_THUMBNAIL_PHOTO);
        } catch (Exception ex) {
            ToastHelper.showShortToast(activityContext, "Camera app is not available");
        }
    }

    /**
     * Create an Image File in the pictures directory, requires xml file paths setup
     *
     * @param context
     * @param picture
     * @return
     * @throws IOException
     */
    private static File createImageFile(Context context, Picture picture) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        picture.setPhotoPath(image.getAbsolutePath());
        return image;
    }

    /**
     * Decode a Scaled Image to match ImageView Size
     *
     * @param imagePreviewImageView
     * @param imageUri
     */
    public static void setImageViewToResizedImage(ImageView imagePreviewImageView, Uri imageUri) {
        // Get the dimensions of the View
        int targetW = imagePreviewImageView.getWidth();
        int targetH = imagePreviewImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imageUri.getPath(), bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), bmOptions);
        imagePreviewImageView.setImageBitmap(bitmap);
    }

    /**
     * Rotate an image if required.
     *
     * @param img                      The image bitmap - original bitmap image, this is done in order to prevent having to access storage in the method
     * @param currentSelectedImagePath Image Path String - this is needed to check the metadata of the picture file
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap rotateImageIfRequired(Bitmap img, String currentSelectedImagePath) throws IOException {

        //Get Meta data and exif information from the given currentSelectedImagePath
        ExifInterface ei = new ExifInterface(currentSelectedImagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    /**
     * Used by rotateImageIfRequired
     *
     * @param img
     * @param degree
     * @return
     */
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * Remove the picture from the pictures folder to reduce storage usage.
     */
    public static void removeCurrentImageFromPicturesFolder(Activity activityContext, String photoPath) {
        File imgFile = new File(photoPath);
        Uri deleteFileUri = FileProvider.getUriForFile(activityContext, COM_TOUCHMEDIAPRODUCTIONS_ANDROID_FILEPROVIDER, imgFile);
        activityContext.getContentResolver().delete(deleteFileUri, null, null);
    }

    /**
     * Straighten the bitmap image in the given picture object
     *
     * @param picture
     */
    private static void straightenImage(Picture picture) {
        File imgFile = new File(picture.getPhotoPath());
        if (imgFile.exists()) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            //Rotate it if necessary:
            try {
                imageBitmap = PictureHelper.rotateImageIfRequired(imageBitmap, picture.getPhotoPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            picture.setPictureBitmap(imageBitmap);
        }
    }

    /**
     * Convert Bitmap to grayscale
     *
     * @param bmpOriginal
     * @return Bitmap converted to ARGB_8888 type as required by Tensorflow
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    /**
     * Method to be used within the onActivityResult of the activity utilising this PictureHelper instance.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public Picture onActivityResultForPicture(int requestCode, int resultCode, @Nullable Intent data) {
        //Load Thumbnail of image into imageview
        if (requestCode == REQUEST_CAPTURE_THUMBNAIL_PHOTO && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            currentPicture.setPictureBitmap(imageBitmap);
            return currentPicture;
        }
        // Load full size Pic via file saved (write) and read to imageview
        else if (requestCode == REQUEST_CAPTURE_FULLSIZE_PHOTO && resultCode == Activity.RESULT_OK) {
            straightenImage(currentPicture);
            //Remove the image from the folder to prevent double storing.
            return currentPicture;
        }
        // Select Image from Gallery load it in imageview, unpackaging image from the data in the intent
        else if (requestCode == REQUEST_PHOTO_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = activityContext.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                currentPicture.setPictureBitmap(bitmap);
                currentPicture.setImgUri(uri);
                return currentPicture;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ImageView to Byte Array
     *
     * @param imageView
     * @return
     */
    public static byte[] imageViewToByteArray(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * Bitmap to Byte Array
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * Byte Array to Bitmap
     *
     * @param byteArray
     * @return
     */
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Rotates the given image in all directions and returns array of rotated images.
     *
     * @param bitmapToRotate
     * @return Rotated images
     */
    public static Bitmap[] rotateBitmapInAllFourDirections(Bitmap bitmapToRotate) {
        int[] angles = new int[]{0, 90, 180, 270};
        Bitmap[] bitmaps = new Bitmap[angles.length];
        for (int i = 0; i < angles.length; i++) {
            bitmaps[i] = rotateBitmap(bitmapToRotate, angles[i]);
        }
        return bitmaps;
    }

    /**
     * Rotate a give bitmap image. Create a new image and return (does not modify original)
     *
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
