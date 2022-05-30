package com.touchmediaproductions.pneumocheck.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class PytorchMLHelper {
    Bitmap bitmap;
    Module module;
    private static final String TAG = "PytorchMLHelper";

    private static PytorchMLHelper instance;


    public static PytorchMLHelper getInstance() {
        if (instance == null) {
            instance = new PytorchMLHelper();
        }
        return instance;
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public MLHelper.Prediction runClassificationOnBitmap(Context context, Bitmap imageBitMap) {
        MLHelper.Prediction prediction = null;
        if (imageBitMap != null) {
            this.bitmap = imageBitMap;
            try {
                module = LiteModuleLoader.load(assetFilePath(context.getApplicationContext(), "covidxray_densenet161.ptl"));

                imageBitMap = Bitmap.createScaledBitmap(imageBitMap,224,224, true);

                // Convert image to pytorch tensor
                Tensor imageTensor = TensorImageUtils.bitmapToFloat32Tensor(imageBitMap, new float[] {0.5f, 0.5f, 0.5f}, new float[] {0.5f, 0.5f, 0.5f});

                Log.i(TAG, "imageTensor: " + Arrays.toString(imageTensor.shape()));

                // Run inference on pytorch tensor
                final Tensor outputTensor = module.forward(IValue.from(imageTensor)).toTensor();

                // Get prediction
                final float[] scores = outputTensor.getDataAsFloatArray();

                Log.i(TAG, "runClassificationOnBitmap: " + Arrays.toString(scores));

                float maxScore = -Float.MAX_VALUE;
                int maxScoreIdx = -1;
                for (int i = 0; i < scores.length; i++) {
                    if (scores[i] > maxScore) {
                        maxScore = scores[i];
                        maxScoreIdx = i;
                    }
                }

                String[] labels;
                // Load from file "labels_pytorch.txt" in assets
                try (InputStream is = context.getAssets().open("labels_pytorch.txt")) {
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    labels = new String(buffer).split("\n");
                }
                String classifiedClassName = labels[maxScoreIdx];

                // Build Prediction Map
                HashMap<String, Float> predictionMap = new HashMap<>();
                for (int i = 0; i < scores.length; i++) {
                    predictionMap.put(labels[i], scores[i]);
                }
                prediction = new MLHelper.Prediction(predictionMap, "local-pytorch-densenet161-covidxray");

                Log.i(TAG, "runClassificationOnBitmap: " + prediction.toString() + " " + classifiedClassName);

            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, e.getMessage());
            }
        }
        return prediction;
    }

}
