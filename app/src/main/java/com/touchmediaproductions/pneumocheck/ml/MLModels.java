package com.touchmediaproductions.pneumocheck.ml;

/**
 * CONSTANTS of all the possible models in the application
 * Used to retreive names and filenames
 */
public enum MLModels {
    MODEL_A_COVIDNET("covidnet_a.tflite"),
    MODEL_B_COVIDNET("covidnet_b.tflite"),
    QUANT_XRAY_MODEL("quant_xray.tflite");

    // internal state
    private String fileName;

    MLModels(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
