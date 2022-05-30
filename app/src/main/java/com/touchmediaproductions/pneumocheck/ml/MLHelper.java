package com.touchmediaproductions.pneumocheck.ml;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Machine Learning Helper Utility class takes care of TensorFlow tasks
 */
public class MLHelper {

    private static final String TAG = "MLHelper";

    private Context context;

    protected Interpreter tflite;

    protected Interpreter firebaseTFLite = null;

    private int imageSizeX;
    private int imageSizeY;

    private TensorImage inputImageBuffer;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;

    private float IMAGE_MEAN;
    private float IMAGE_STD;
    private String MODELFILENAME;
    private String MODELDISPLAYNAME;

    //Set defaults
    private float PROBABILITY_MEAN = 0.0f;
    private float PROBABILITY_STD = 1.0f;
    private String LABEL_FILE_NAME = "labels.txt";


    private List<String> LABELS_LIST;

    //Classification underway flag makes sure that classification does not run again till its finished
    private boolean CLASSIFICATION_RUNNING = false;

    private MLHelper(Context context, MLModels model) throws IOException {
        this.context = context;

        //Depending on the given model loaded set the variables and labels to be used
        switch (model) {
            case MODEL_A_COVIDNET:
            case MODEL_B_COVIDNET:
                this.PROBABILITY_MEAN = 0.0f;
                this.PROBABILITY_STD = 1.0f;
                this.IMAGE_MEAN = 127.5f;
                this.IMAGE_STD = 127.5f;
                this.LABEL_FILE_NAME = "labels_covidnet.txt";
                //Use firebase ML for the covidnet models
                firebaseTFLite = FirebaseCXRayMLHelper.getInstance().getFirebaseTFLite();
                break;
//            case FLOAT_XRAY_MODEL:
//                this.PROBABILITY_MEAN = 0.0f;
//                this.PROBABILITY_STD = 1.0f;
//                this.IMAGE_MEAN = 127.5f;
//                this.IMAGE_STD = 127.5f;
//                this.LABEL_FILE_NAME = "labels_xray.txt";
//                break;
            case QUANT_XRAY_MODEL:
                this.PROBABILITY_MEAN = 0.0f;
                this.PROBABILITY_STD = 255.0f;
                this.IMAGE_MEAN = 0.0f;
                this.IMAGE_STD = 1.0f;
                this.LABEL_FILE_NAME = "labels_xray.txt";
                break;
        }

        this.MODELFILENAME = model.getFileName();
        if (firebaseTFLite != null) {
            this.MODELDISPLAYNAME = FirebaseCXRayMLHelper.getInstance().getModelName();
        } else {
            this.MODELDISPLAYNAME = model.getFileName().replace(".tflite", "");
        }

        Interpreter.Options interpreterOptions = new Interpreter.Options();
        tflite = new Interpreter(loadmodelfile((Activity) this.context), interpreterOptions);

    }

    private Prediction runClassification(Bitmap bitmap) {
        if (!CLASSIFICATION_RUNNING) {
            CLASSIFICATION_RUNNING = true;
            Log.i("TFModel", "Classification initiated...");

            int imageTensorIndex = 0;
            int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape();
            imageSizeX = imageShape[1];
            imageSizeY = imageShape[2];
            DataType imageDataType = tflite.getOutputTensor(imageTensorIndex).dataType();

            int probabilityTensorIndex = 0;
            int[] probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape();
            DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

            inputImageBuffer = new TensorImage(imageDataType);
            outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
            probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

            inputImageBuffer = loadImage(bitmap);

            if (firebaseTFLite == null) {
                Log.i(TAG, "Using Local Model for " + MODELDISPLAYNAME);
                tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
            } else {
                Log.i(TAG, "Attempting to use firebase downloaded model. " + MODELDISPLAYNAME);
                firebaseTFLite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
            }

            CLASSIFICATION_RUNNING = false;
            return getResult();
        } else {
            Log.i("TFModel", "Classification is already running.");
        }
        return null;
    }

    /**
     * Load the model by MODELNAME
     *
     * @param activity
     * @return
     * @throws IOException
     */
    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODELFILENAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }

    /**
     * Load the image into the classifier and convert it to a TensorImage essentially a numeric representation/buffer
     *
     * @param bitmap
     * @return
     */
    private TensorImage loadImage(final Bitmap bitmap) {
        inputImageBuffer.load(bitmap);
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        // The model was trained with BILINEAR Resize as python's csv2.resize() interpolation is Bilinear
                        // https://www.tutorialkart.com/opencv/python/opencv-python-resize-image/
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        Log.i("IMAGESIZE", "Image size Before Processing : " + inputImageBuffer.getWidth() + " x " +
                inputImageBuffer.getHeight());
        TensorImage processedImage = imageProcessor.process(inputImageBuffer);
        Log.i("IMAGESIZE", "Image size after Processing : " + processedImage.getWidth() + " x " +
                processedImage.getHeight());
        return processedImage;
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    private TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private Prediction getResult() {
        if (firebaseTFLite != null) {
            LABELS_LIST = FirebaseCXRayMLHelper.getInstance().getModelLabels();
        } else {
            try {
                LABELS_LIST = FileUtil.loadLabels(context, LABEL_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, Float> labeledProbability = (HashMap<String, Float>) new TensorLabel(LABELS_LIST, probabilityProcessor.process(outputProbabilityBuffer)).getMapWithFloatValue();
        tflite.close();

        return new Prediction(labeledProbability, MODELDISPLAYNAME);
    }


    /**
     * Prediction object that holds top three prediction and their values.
     * Serialized to allow saving in DB and passing through in Intent Bundle extras
     */
    public static class Prediction implements Serializable {
        private String first = "";
        private String second = "";
        private String third = "";
        private float firstValue = 0;
        private float secondValue = 0;
        private float thirdValue = 0;
        private String modelName;

        private HashMap<String, Float> sortedProbabilities;

        public Prediction(HashMap<String, Float> probabilityMap) {
            this.sortedProbabilities = sortByValue(probabilityMap);
            Object[] keyset = sortedProbabilities.keySet().toArray();
            if (sortedProbabilities.size() > 0) {
                this.first = (String) keyset[0];
                this.firstValue = sortedProbabilities.get(this.first) * 100;
            }
            if (sortedProbabilities.size() > 1) {
                this.second = (String) keyset[1];
                this.secondValue = sortedProbabilities.get(this.second) * 100;
            }
            if (sortedProbabilities.size() > 2) {
                this.third = (String) keyset[2];
                this.thirdValue = sortedProbabilities.get(this.third) * 100;
            }
        }

        public Prediction(HashMap<String, Float> probabilityMap, String modelName) {
            this(probabilityMap);
            this.modelName = modelName;
        }

        private static HashMap<String, Float> sortByValue(HashMap<String, Float> unsortMap) {
            // 1. Convert Map to List of Map
            List<Map.Entry<String, Float>> list =
                    new LinkedList<>(unsortMap.entrySet());
            // 2. Sort list with Collections.sort() using customer Comparator
            Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
                public int compare(Map.Entry<String, Float> o1,
                                   Map.Entry<String, Float> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
            // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
            HashMap<String, Float> sortedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Float> entry : list) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            return sortedMap;
        }

        @Override
        public String toString() {
            String details = "AI Prediction: " + (int) this.getFirstValue()
                    + "% " + this.getFirst()
                    + " | " + (int) this.getSecondValue()
                    + "% " + this.getSecond()
                    + " | " + (int) this.getThirdValue()
                    + "% " + this.getThird();
            return details;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        public String getThird() {
            return third;
        }

        public float getFirstValue() {
            return firstValue;
        }

        public float getSecondValue() {
            return secondValue;
        }

        public float getThirdValue() {
            return thirdValue;
        }

        public HashMap<String, Float> getSortedProbabilities() {
            return (HashMap<String, Float>) sortedProbabilities;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
    }


    /**
     * Run the classification on the given image.
     *
     * @param context     Activity context to run on.
     * @param imageBitMap Bitmap Image to run the classification against.
     * @param modelToUse  Model to use
     * @return
     */
    public static MLHelper.Prediction runClassificationOnBitmap(Context context, Bitmap imageBitMap, MLModels modelToUse) {
        MLHelper.Prediction prediction = null;
        if (imageBitMap != null) {
            try {
                //Prepare the Machine Learning Helper
                MLHelper mlHelper = new MLHelper(context, modelToUse);

                //Run Classification against bitmap image input:
                prediction = mlHelper.runClassification(imageBitMap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ToastHelper.showShortToast(context, "Please choose a photo first.");
        }
        return prediction;
    }

}
