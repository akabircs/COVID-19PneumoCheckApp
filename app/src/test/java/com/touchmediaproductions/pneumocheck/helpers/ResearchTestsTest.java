package com.touchmediaproductions.pneumocheck.helpers;

import junit.framework.TestCase;

public class ResearchTestsTest extends TestCase {

    public void testConfidenceInterval() {
        double z = 1.96d;
        double p = 29 / 50d;
        double n = 50d;
        double ci = z * Math.sqrt(((p * 1) + (p * -p)) / n);
        // ci rounded off to 4 decimal points
        float rounded = (float) Math.round(ci * 10000) / 10000;
        System.out.println("CI: " + rounded);
        // Printer upper and lower
        System.out.println("Upper: " + (p + ci));
        System.out.println("Lower: " + (p - ci));
    }

}