package com.touchmediaproductions.pneumocheck.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.touchmediaproductions.pneumocheck.ml.MLHelper;
//import com.touchmediaproductions.pneumocheck.ml.PytorchMLHelper;

//import org.pytorch.IValue;
//import org.pytorch.LiteModuleLoader;
//import org.pytorch.Module;
//import org.pytorch.Tensor;
//import org.pytorch.torchvision.TensorImageUtils;

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



}
