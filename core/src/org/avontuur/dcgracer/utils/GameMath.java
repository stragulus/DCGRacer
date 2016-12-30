package org.avontuur.dcgracer.utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * This class contains game-related math functions.
 *
 * Created by Bram Avontuur on 2016-03-07.
 */
public class GameMath {
    private static Random random = new Random();

    /**
     * Generates a list of values to render a 2D side-view landscape with random inclinations (height map)
     *
     * @param numIterations
     *        This determines how many data points the returning list will have, which is 2^numIterations+1.
     * @param range
     *        This determines the range (maximum Y value) for the data points
     * @param firstY
     *        Y value of first coordinate. Useful for linking up multiple segments.
     * @param lastY
     *        Y value of last coordinate. Useful for linking up multiple segments.
     * @return
     *        Returns an array of floats. The indices are the X coordinates, the values are the Y coordinates. The
     *        Y coordinates are of value 0 <= Y <= range
     */
    public static float[] midpointDisplacement2D(final int numIterations, final float range, final float firstY,
                                                 final float lastY, final float roughness) {
        int numElements = (int)Math.pow(2, numIterations) + 1;
        float[] result = new float[numElements];
        result[0] = firstY;
        result[numElements - 1] = lastY;

        calculateMidpoint(result, 0, numElements - 1, range / 2, roughness);
        return result;
    }

    /**
     * Calculates a random value in a given range.
     * TODO: add 'roughness' parameter
     *
     * @return A random value between start (inclusive) and end (exclusive)
     */
    public static float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

    /**
     *
     * @param values
     *        Array with y-coordinates
     * @param firstIndex
     *        Index in values of left side of segment
     * @param lastIndex
     *        Index in values of right side of segment
     * @param range
     *        Maximum value of random offset to be applied to middle of segment
     * @param roughness
     *        How 'rough' the terrain will look (0 < roughness < 1). Closer to 1 means rougher.
     */
    private static void calculateMidpoint(float[] values, final int firstIndex, final int lastIndex,
                                          final float range, final float roughness) {
        // Recursive implementation of the midfield displacement algorithm that generates a 2-dimensional terrain.
        // TODO: write a test for this function!
        //float midPoint = MathUtils.clamp((values[firstIndex] + values[lastIndex]) / 2f + random(-range, range), 0,
        //        range * 2);
        float midPoint = (values[firstIndex] + values[lastIndex]) / 2f + random(-range, range);
        int midIndex = firstIndex + ((lastIndex - firstIndex) / 2);
        values[midIndex] = midPoint;
        if (midIndex != firstIndex + 1) {
            calculateMidpoint(values, firstIndex, midIndex, range * roughness, roughness);
            calculateMidpoint(values, midIndex, lastIndex, range * roughness, roughness);
        }
    }
}
