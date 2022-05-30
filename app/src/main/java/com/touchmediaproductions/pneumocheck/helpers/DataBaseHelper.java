package com.touchmediaproductions.pneumocheck.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Class responsible for communicating and manipulating the Database
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    // Database Name Constant
    public static final String DATABASE_FILENAME = "PneumoCheck.db";

    private static final String TABLE_NAME = "XRAY_SUBMISSION_TABLE";
    private static final String COLUMN_XRAY_ID = "XRAY_ID";
    private static final String COLUMN_FIRSTNAME = "FIRSTNAME";
    private static final String COLUMN_LASTNAME = "LASTNAME";
    private static final String COLUMN_AGE = "AGE";
    private static final String COLUMN_SEX = "SEX";
    private static final String COLUMN_SCAN_CREATION_DATE = "SCAN_CREATION_DATE";
    private static final String COLUMN_SUBMISSION_CREATION_DATE = "SUBMISSION_CREATION_DATE";
    //Confirmed prediction should be either unconfirmed, covid-19, pneumonia, normal
    private static final String COLUMN_CONFIRMEDPREDICTION = "CONFIRMED_PREDICTION";
    private static final String COLUMN_PREDICTIONBLOB = "PREDICTION_BLOB";
    private static final String COLUMN_CXRPHOTO = "CXR_PHOTO";
    private static final String TAG = "DataBaseHelper";


    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_FILENAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_XRAY_ID + " INTEGER, " + COLUMN_FIRSTNAME + " TEXT, " + COLUMN_LASTNAME + " TEXT, "
                + COLUMN_AGE + " INT, " + COLUMN_SEX + " TEXT, " + COLUMN_SCAN_CREATION_DATE + " TEXT, " + COLUMN_SUBMISSION_CREATION_DATE + " TEXT, " + COLUMN_CONFIRMEDPREDICTION + " TEXT, " + COLUMN_PREDICTIONBLOB + " BLOB, " + COLUMN_CXRPHOTO + " BLOB )";
        sqLiteDatabase.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean updateSubmission(SubmissionModel updatedSubmission) {
        // Prepare values
        Date scanCreationDate = updatedSubmission.getScanCreationDate();
        Date submissionCreationDate = updatedSubmission.getSubmissionCreationDate();

        String scanCreationString = DateHelper.fromDateToISO8601String(scanCreationDate);
        String submissionCreationString = DateHelper.fromDateToISO8601String(submissionCreationDate);

        ContentValues values = new ContentValues();
        values.put(COLUMN_XRAY_ID, updatedSubmission.getId());
        values.put(COLUMN_FIRSTNAME, updatedSubmission.getFirstName());
        values.put(COLUMN_LASTNAME, updatedSubmission.getLastName());
        values.put(COLUMN_AGE, updatedSubmission.getAge());
        values.put(COLUMN_SEX, updatedSubmission.getSex());
        values.put(COLUMN_SCAN_CREATION_DATE, scanCreationString);
        values.put(COLUMN_SUBMISSION_CREATION_DATE, submissionCreationString);
        values.put(COLUMN_CONFIRMEDPREDICTION, updatedSubmission.getConfirmation().name());
        values.put(COLUMN_PREDICTIONBLOB, predictionToByteArray(updatedSubmission.getPrediction()));
        values.put(COLUMN_CXRPHOTO, updatedSubmission.getCxrPhoto());
        //Open DB
        SQLiteDatabase db = this.getWritableDatabase();
        int updateResult = db.update(TABLE_NAME, values, COLUMN_XRAY_ID + " = ?", new String[]{String.valueOf(updatedSubmission.getId())});
        db.close();
        if (updateResult == -1) {
            return false;
        } else {
            return true;
        }
    }

    public static byte[] predictionToByteArray(MLHelper.Prediction prediction) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(prediction);
            byte[] employeeAsBytes = baos.toByteArray();
            return employeeAsBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Simple lightweight check to check if DB table is empty.
     *
     * @return
     */
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        Boolean isEmpty;
        if (mCursor != null && mCursor.moveToFirst()) {
            isEmpty = false;
        } else {
            isEmpty = true;
        }
        return isEmpty;
    }

    /**
     * Convert from byteArray serialised Prediction object to Prediction Object
     *
     * @param data byteArray serialised Prediction object
     * @return MLHelper.Prediction object recovered from byte[] data given
     */
    public static MLHelper.Prediction byteArrayBlobToPrediction(byte[] data) {
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            MLHelper.Prediction dataobj = (MLHelper.Prediction) ois.readObject();
            return dataobj;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}

