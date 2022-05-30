package com.touchmediaproductions.pneumocheck.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.ml.PytorchMLHelper;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

public class ResearchTests {
    // Format numbers to 4 decimal places
    final static DecimalFormat DF = new DecimalFormat("#.####");

    private static final String TAG = "ResearchTests";

    enum TestType {
        Inference, Loading;
    }

    public static class Test {
        private float timeTaken = 0;
        private float startTime = 0;
        private float endTime = 0;
        private final TestType testType;

        public Test(TestType testType) {
            this.testType = testType;
        }

        public void start() {
            // Nano seconds
            startTime = System.nanoTime();
        }

        public float end() {
            // Nano seconds
            endTime = System.nanoTime();
            timeTaken = endTime - startTime;
            return this.timeTaken;
        }

        public float getEndTime() {
            return this.endTime;
        }

        public float getTimeTaken() {
            return this.timeTaken;
        }

        public float getTimeTakenInSeconds() {
            return this.timeTaken / 1000000000;
        }

        public TestType getTestType() {
            return this.testType;
        }
    }

    public static class LoadTimeTest extends Test {
        public LoadTimeTest() {
            super(TestType.Loading);
        }
    }

    public static class InferenceTimeTest extends Test {
        private MLHelper.Prediction predictionObject;
        private String correctClass;
        private float[] probabilities;

        public InferenceTimeTest() {
            super(TestType.Inference);
        }

        public void setPredictionObject(MLHelper.Prediction predictionObject) {
            this.predictionObject = predictionObject;
        }

        public void setCorrectClass(String correctClass) {
            this.correctClass = correctClass;
        }

        public void setProbabilities(float[] probabilities) {
            this.probabilities = probabilities;
        }

        public MLHelper.Prediction getPredictionObject() {
            return this.predictionObject;
        }

        public String getCorrectClass() {
            return this.correctClass;
        }

        public float[] getProbabilities() {
            return this.probabilities;
        }

        public boolean isCorrect() {
            return this.predictionObject.getFirst().equals(this.correctClass);
        }
    }

    public static double calculateAverageTimeTaken(Test[] tests) {
        double totalTime = 0;
        for (Test test : tests) {
            totalTime += test.getTimeTaken();
        }
        return totalTime / tests.length;
    }

    public static double calculateStandardDeviation(Test[] tests) {
        double averageTime = calculateAverageTimeTaken(tests);
        float totalTime = 0;
        for (Test test : tests) {
            totalTime += (test.getTimeTaken() - averageTime) * (test.getTimeTaken() - averageTime);
        }
        return (double) Math.sqrt(totalTime / tests.length);
    }

    public static int calculateInferenceCorrectCount(InferenceTimeTest[] tests) {
        int correctCount = 0;
        for (InferenceTimeTest test : tests) {
            if (test.isCorrect()) {
                correctCount++;
            }
        }
        return correctCount;
    }

    public static double calculateInferenceAccuracy(InferenceTimeTest[] tests) {
        return  calculateInferenceCorrectCount(tests) / (double) tests.length;
    }

    // Calculate Binomial Confidence Interval
    public static double calculateInferenceAccuracy95ci(InferenceTimeTest[] tests) {
        double z = 1.96d;
        double p = (double) calculateInferenceAccuracy(tests);
        double n = (double) tests.length;
        double ci = z * Math.sqrt(((p * 1) + (p * -p)) / n);
        return ci;
    }

//    public static String returnTestResultsInNanoSecondsAndSeconds(InferenceTimeTest[] tests){
//        double averageTime = calculateAverageTimeTaken(tests);
//        double standardDeviation = calculateStandardDeviation(tests);
//        double accuracy = calculateInferenceAccuracy(tests);
//        double confidenceInterval = calculateInferenceAccuracy95ci(tests);
//        String testType = "";
//        if (tests.length > 0) {
//            testType = tests[0].getTestType().name();
//        }
//        return testType + " Test\nAverage time taken: " + averageTime + "ns (" + averageTime / 1000000000 + "secs)\n" +
//                "Standard Deviation: " + standardDeviation + "ns (" + standardDeviation / 1000000000 + "secs)\n" +
//                "Accuracy: " + accuracy + "\n" +
//                "95% Confidence Interval: " + confidenceInterval + "\n";
//    }
//
//    public static String returnTestResultsInNanoSecondsAndSeconds(Test[] tests) {
//        double averageTime = calculateAverageTimeTaken(tests);
//        double standardDeviation = calculateStandardDeviation(tests);
//        String testType = "";
//        if (tests.length > 0) {
//            testType = tests[0].getTestType().name();
//        }
//        return   testType + " Test\nAverage time taken: " + averageTime + "ns (" + averageTime / 1000000000 + "secs)\n" +
//                "Standard Deviation: " + standardDeviation + "ns (" + standardDeviation / 1000000000 + "secs)\n";
//    }

    public static class LoadTimeTestResults {
        private LoadTimeTest[] loadTimeTests = new LoadTimeTest[0];
        private double averageTimeTakenNanoSeconds = 0;
        private double standardDeviationTimeTakenNanoSeconds = 0;
        private double averageTimeTakenInSeconds = 0;
        private double standardDeviationTimeTakenInSeconds = 0;
        final private TestType testType = TestType.Loading;

        public void setLoadTimeTests(LoadTimeTest[] loadTimeTests) {
            this.loadTimeTests = loadTimeTests;
            this.averageTimeTakenNanoSeconds = calculateAverageTimeTaken(loadTimeTests);
            this.standardDeviationTimeTakenNanoSeconds = calculateStandardDeviation(loadTimeTests);
            this.averageTimeTakenInSeconds = averageTimeTakenNanoSeconds / 1000000000;
            this.standardDeviationTimeTakenInSeconds = standardDeviationTimeTakenNanoSeconds / 1000000000;
        }

        public LoadTimeTest[] getLoadTimeTests() {
            return loadTimeTests;
        }

        public double getAverageTimeTakenNanoSeconds() {
            return averageTimeTakenNanoSeconds;
        }

        public double getStandardDeviationTimeTakenNanoSeconds() {
            return standardDeviationTimeTakenNanoSeconds;
        }

        public double getAverageTimeTakenInSeconds() {
            return averageTimeTakenInSeconds;
        }

        public double getStandardDeviationTimeTakenInSeconds() {
            return standardDeviationTimeTakenInSeconds;
        }

        public String getLoadTimeTestsAsCSV() {
            StringBuilder sb = new StringBuilder();
            sb.append("Test Type,Average Time Taken (secs)\n");
            for (LoadTimeTest test : loadTimeTests) {
                sb.append(testType.name() + "," + DF.format(test.getTimeTakenInSeconds()) + "\n");
            }
            return sb.toString();
        }

        public String getLoadTimeResultsAsCSV() {
            StringBuilder sb = new StringBuilder();
            sb.append("Test Type,Average Time Taken (secs),Standard Deviation (secs)\n");
            sb.append(testType.name() + "," + DF.format(averageTimeTakenInSeconds) + "," + DF.format(standardDeviationTimeTakenInSeconds) + "\n");
            return sb.toString();
        }
    }

    public static class InferenceTimeTestResults {
        private InferenceTimeTest[] inferenceTimeTests = new InferenceTimeTest[0];
        private double averageTimeTakenNanoSeconds = 0;
        private double standardDeviationTimeTakenNanoSeconds = 0;
        private double averageTimeTakenInSeconds = 0;
        private double standardDeviationTimeTakenInSeconds = 0;
        final private TestType testType = TestType.Inference;
        private double accuracy = 0;
        private double confidenceInterval = 0;

        public void setInferenceTimeTests(InferenceTimeTest[] inferenceTimeTests) {
            this.inferenceTimeTests = inferenceTimeTests;
            this.averageTimeTakenNanoSeconds = calculateAverageTimeTaken(inferenceTimeTests);
            this.standardDeviationTimeTakenNanoSeconds = calculateStandardDeviation(inferenceTimeTests);
            this.averageTimeTakenInSeconds = averageTimeTakenNanoSeconds / 1000000000;
            this.standardDeviationTimeTakenInSeconds = standardDeviationTimeTakenNanoSeconds / 1000000000;
            this.accuracy = calculateInferenceAccuracy(inferenceTimeTests);
            this.confidenceInterval = calculateInferenceAccuracy95ci(inferenceTimeTests);
        }

        public InferenceTimeTest[] getInferenceTimeTests() {
            return inferenceTimeTests;
        }

        public double getAverageTimeTakenNanoSeconds() {
            return averageTimeTakenNanoSeconds;
        }

        public double getStandardDeviationTimeTakenNanoSeconds() {
            return standardDeviationTimeTakenNanoSeconds;
        }

        public double getAverageTimeTakenInSeconds() {
            return averageTimeTakenInSeconds;
        }

        public double getStandardDeviationTimeTakenInSeconds() {
            return standardDeviationTimeTakenInSeconds;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public double getConfidenceInterval() {
            return confidenceInterval;
        }

        public String getInferenceTimeTestsAsCSV() {
            StringBuilder sb = new StringBuilder();
            sb.append("Test Type,Time Taken (secs),Prediction,Actual\n");
            for (InferenceTimeTest test : inferenceTimeTests) {
                sb.append(testType.name() + "," + DF.format(test.getTimeTakenInSeconds()) + "," + test.getPredictionObject().getFirst() + "," + test.getCorrectClass() + "\n");
            }
            return sb.toString();
        }

        public String getInferenceTimeResultsAsCSV() {
            StringBuilder sb = new StringBuilder();
            sb.append("Test Type,Average Time Taken (secs),Standard Deviation (secs),Accuracy,95% Confidence Interval\n");
            sb.append(testType.name() + "," + DF.format(averageTimeTakenInSeconds) + "," + DF.format(standardDeviationTimeTakenInSeconds) + "," + DF.format(accuracy) + "," + DF.format(confidenceInterval) + "\n");
            return sb.toString();
        }
    }


    // Loading Test
    public static LoadTimeTestResults runLoadingTest(Context context, String modelFilePath, int numTests) {
        Log.i(TAG, "Running Loading Test");
        LoadTimeTest[] loadTimeTests = new LoadTimeTest[numTests];
        for (int i = 0; i < numTests; i++) {
            // Log progressing loading time
            if (i % 10 == 0) {
                Log.i(TAG, "Loading Test: " + i + " of " + numTests);
            }

            loadTimeTests[i] = new LoadTimeTest();
            loadTimeTests[i].start();
            Module model = null;
            try {
                model = LiteModuleLoader.load(PytorchMLHelper.assetFilePath(context.getApplicationContext(), modelFilePath));
            } catch (IOException e) {
                Log.e(TAG, "Error loading model: " + e.getMessage());
                e.printStackTrace();
            }
            loadTimeTests[i].end();
            if (model != null) {
                model.destroy();
            }
        }
        LoadTimeTestResults loadTimeTestResults = new LoadTimeTestResults();
        loadTimeTestResults.setLoadTimeTests(loadTimeTests);
        Log.i(TAG, "Finished Loading Test");
        return loadTimeTestResults;
    }

    // Inference Test
    public static InferenceTimeTestResults runInferenceTest(Context context, String modelFilePath, TestImageSetInferenceHelper.ImageSet[] imageSets) {
        Log.i(TAG, "Running Inference Test");
        InferenceTimeTest[] inferenceTimeTests = new InferenceTimeTest[imageSets.length];
        try {
            Module model = LiteModuleLoader.load(PytorchMLHelper.assetFilePath(context.getApplicationContext(), modelFilePath));

            // Run inference on 10 images first to warm up
            for (int j = 0; j < 10; j++) {
                // Log dummy process percentage
                Log.i(TAG, "Who cares - Dummy warm up processing image " + j + " of " + imageSets.length);

                // Get full path from assets folder path
                String assetsFolderPath = imageSets[j].path;
                Bitmap bitmapImage = BitmapFactory.decodeStream(context.getAssets().open(assetsFolderPath));
                bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 224, 224, true);
                // Convert image to pytorch tensor
                Tensor imageTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmapImage, new float[]{0.5f, 0.5f, 0.5f}, new float[]{0.5f, 0.5f, 0.5f});
                Log.i(TAG, "whoCaresImageTensor: " + Arrays.toString(imageTensor.shape()));
                Tensor whoCaresOutput = model.forward(IValue.from(imageTensor)).toTensor();
                Log.i(TAG, "whoCaresOutput: " + Arrays.toString(whoCaresOutput.shape()));
            }

            for (int i = 0; i < imageSets.length; i++) {
                // Log process percentage
                if (i % 10 == 0) {
                    Log.i(TAG, "Processing image " + i + " of " + imageSets.length);
                }

                // Get full path from assets folder path
                String assetsFolderPath = imageSets[i].path;
                Bitmap bitmapImage = BitmapFactory.decodeStream(context.getAssets().open(assetsFolderPath));
                bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 224, 224, true);
                // Convert image to pytorch tensor
                Tensor imageTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmapImage, new float[]{0.5f, 0.5f, 0.5f}, new float[]{0.5f, 0.5f, 0.5f});
                // Log.i(TAG, "imageTensor: " + Arrays.toString(imageTensor.shape()));

                // START TIMER
                inferenceTimeTests[i] = new InferenceTimeTest();
                inferenceTimeTests[i].start();
                // Run inference
                Tensor outputTensor = model.forward(IValue.from(imageTensor)).toTensor();
                inferenceTimeTests[i].end();
                // END TIMER

                // Get prediction
                final float[] scores = outputTensor.getDataAsFloatArray();
                // Log.i(TAG, "runClassificationOnBitmap: " + Arrays.toString(scores));

                float maxScore = -Float.MAX_VALUE;
                int maxScoreIdx = -1;
                for (int x = 0; x < scores.length; x++) {
                    if (scores[x] > maxScore) {
                        maxScore = scores[x];
                        maxScoreIdx = x;
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
                for (int z = 0; z < scores.length; z++) {
                    predictionMap.put(labels[z], scores[z]);
                }
                MLHelper.Prediction prediction = new MLHelper.Prediction(predictionMap, "local-pytorch-densenet161-covidxray");

                if (!prediction.getFirst().equals(classifiedClassName)) throw new AssertionError();

                inferenceTimeTests[i].setPredictionObject(prediction);
                inferenceTimeTests[i].setCorrectClass(imageSets[i].diagnosis);

                // Log.i(TAG, "Is Correct?: " + inferenceTimeTests[i].isCorrect());
            }
            InferenceTimeTestResults inferenceTimeTestResults = new InferenceTimeTestResults();
            inferenceTimeTestResults.setInferenceTimeTests(inferenceTimeTests);
            Log.i(TAG, "Finished Inference Test");
            return inferenceTimeTestResults;
        } catch (IOException e) {
            Log.e(TAG, "Error Inference Testing model: " + e.getMessage());
            e.printStackTrace();
        }
        Log.i(TAG, "Finished Inference Test With Error");
        return new InferenceTimeTestResults();
    }


}
