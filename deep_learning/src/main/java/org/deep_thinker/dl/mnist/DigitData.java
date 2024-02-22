package org.deep_thinker.dl.mnist;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Random;

import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.*;

/**
 * Holds data for a digit as well as its label.
 */
public class DigitData {

    private static final double[][] EXPECTED_ARRAY = new double[][]{
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1}
    };
    private double[] data;
    private double[] transformedData;
    private int label;
    private Random rnd = new Random();

    public DigitData(double[] data, int label) {
        this.data = data;
        this.label = label;
    }

    public double[] getData() {
        return transformedData != null ? transformedData : data;
    }

    public int getLabel() {
        return label;
    }

    public double[] getLabelAsArray() {
        return EXPECTED_ARRAY[label];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        String line = " ---------------------------- ";
        sb.append("Label: ").append(label)
                .append("  (").append(Arrays.toString(getLabelAsArray()))
                .append("\n").append(line);
        int cnt = 0;
        double[] srcData = transformedData != null ? transformedData : data;
        for (int r = 0; r < 28; r++) {
            sb.append("\n|");
            for (int c = 0; c < 28; c++) {
                sb.append(toChar(srcData[cnt++]));
            }
            sb.append("|");
        }

        sb.append("\n").append(line).append("\n");
        return sb.toString();
    }

    /**
     * Simply converts gray-scale to an ascii-shade
     */
    private char toChar(double val) {
        return " .:-=+*#%@".charAt(max(min((int) (val * 10), 9), 0));
    }


    /**
     * Creates a slightly modified version of the original digit.
     */
    public void transformDigit() {

        try {
            double[] dst = new double[data.length];
            boolean potentialOverspill;
            int overspillCounter = 0;

            do {
                potentialOverspill = false;

                AffineTransform t = getTranslateInstance(14, 14);
                t.rotate(toRadians(rnd() * 20));
                t.scale(rnd() * 0.25 + 1, rnd() * 0.25 + 1);
                t.translate(-14 + (rnd() * 3), -14 + (rnd() * 3));

                Point2D wPoint = new Point2D.Double();
                Point2D rPoint = new Point2D.Double();

                looping:
                for (int y = 0; y < 28; y++) {
                    for (int x = 0; x < 28; x++) {
                        wPoint.setLocation(x, y);
                        t.inverseTransform(wPoint, rPoint);

                        clamp(rPoint, 0, 28);

                        // integer part
                        int xi = (int) rPoint.getX();
                        int yi = (int) rPoint.getY();

                        // fractional part
                        double xf = rPoint.getX() - xi;
                        double yf = rPoint.getY() - yi;

                        double interpolatedValue =
                                (1 - xf) * (1 - yf) * pixelValue(xi, yi, data) +
                                (1 - xf) * yf * pixelValue(xi, yi + 1, data) +
                                xf * (1 - yf) * pixelValue(xi + 1, yi, data) +
                                xf * yf * pixelValue(xi + 1, yi + 1, data);

                        if (interpolatedValue > 0 && onBorder(x, y)) {
                            potentialOverspill = true;
                            overspillCounter++;
                            break looping;
                        }

                        dst[y * 28 + x] = interpolatedValue;
                    }
                }

            } while (potentialOverspill && overspillCounter < 5);

            if (overspillCounter < 5)
                transformedData = dst;

        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Should not happen: ", e);
        }
    }

    private boolean onBorder(int x, int y) {
        return x == 0 || y == 0 || x == 27 || y == 27;
    }

    private double pixelValue(int x, int y, double[] data) {
        return data[min(y * 28 + x, data.length - 1)];
    }

    private void clamp(Point2D point, int min, int max) {
        point.setLocation(min(max(point.getX(), min), max), min(max(point.getY(), min), max));
    }

    private double rnd() {
        return rnd.nextDouble() - 0.5;
    }

    public void setRandom(Random rnd) {
        this.rnd = rnd;
    }

}
