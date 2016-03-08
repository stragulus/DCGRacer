package org.avontuur.dcgracer.utils;

import java.util.Random;

/**
 * This class contains game-related math functions.
 *
 * Created by Bram Avontuur on 2016-03-07.
 */
public class GameMath {
    private static Random random = new Random();

    /**
     * Generates a list of values to render a 2D side-view landscape with random inclinations
     * @param numIterations
     *        This determines how many data points the returning list will have, which is 2^numIterations+1.
     * @param range
     *        This determines the range (maximum Y value) for the data points
     * @return
     *        Returns an array of floats. The indices are the X coordinates, the values are the Y coordinates. The
     *        Y coordinates are of value 0 <= Y <= range
     */
    public static float[] midfieldDisplacement2D(final int numIterations, final float range) {
        int numElements = (int)Math.pow(2, numIterations) + 1;
        float[] result = new float[numElements];
        result[0] = range / 2;
        result[numElements - 1] = range / 2;

        calculateMidfield(result, 0, numElements - 1, range / 2);
        return result;
    }

    /**
     * Calculates a random value in a given range.
     *
     * @return A random value between start (inclusive) and end (exclusive)
     */
    public static float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

    private static void calculateMidfield(float[] values, final int firstIndex, final int lastIndex,
                                          final float range) {
        // Recursive implementation of the midfield displacement algorithm that generates a 2-dimensional terrain.
        // TODO: write a test for this function!
        float midPoint = (values[firstIndex] + values[lastIndex]) / 2f + random(-range, range);
        int midIndex = firstIndex + ((lastIndex - firstIndex) / 2);
        values[midIndex] = midPoint;
        if (midIndex != firstIndex + 1) {
            calculateMidfield(values, firstIndex, midIndex, range / 2);
            calculateMidfield(values, midIndex, lastIndex, range / 2);
        }
    }
}
