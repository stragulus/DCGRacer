package org.avontuur.dcgracer.utils;

/**
 * Contains methods that generate 2D terrains.
 *
 * Created by Bram Avontuur on 2016-04-04.
 */
public abstract class TerrainGenerator {

    /**
     * Generates a 2D side-view natural looking terrain.
     * @param numIterations
     *        Number of iterations to us to generate the terrain. More iterations give exponentially more data points,
     *        but it also smooths the terrain more.
     * @param range
     *        Upper vertical range of the terrain data.
     * @param scaleX
     *        Multiply x coordinates by this factor; essentially moves data points closer together (<1)
     *        or further apart (>1).
     * @param scaleY
     *        Multiple y coordinates by this factor; allows one to stretch (>1) or condense (<1) the vertical range.
     * @param xOffset
     *        X value of first coordinate.
     * @param firstY
     *        Y value of first coordinate, with scaleY already applied.
     * @param roughness
     *        How rough the terrain should look. 0 < roughness < 1. Closer to 1 implies rougher terrain.
     * @return
     *        Float array containing terrain data, alternating between x and y coordinates. Number of coordinates
     *        equals (2^numIterations + 1).
     */
    public static float[] generateTerrainData(final int numIterations, final float range, final float scaleX,
                                              final float scaleY, final float xOffset,
                                              final float firstY, final float roughness) {
        float[] terrainDataPointsRaw = GameMath.midpointDisplacement2D(numIterations, range, firstY / scaleY,
                (float)Math.random() * range, roughness);
        // + 3 * 2: adding vertices to make it a closed simple polygon so it can be filled with a background texture
        float[] terrainDataPoints = new float[terrainDataPointsRaw.length * 2];

        // Convert to array of alternating x,y coordinates
        for (int i = 0; i < terrainDataPointsRaw.length; i++) {
            float x = xOffset + (float)i * scaleX;
            float y = terrainDataPointsRaw[i] * scaleY;
            terrainDataPoints[i * 2] = x;
            terrainDataPoints[i * 2 + 1] = y;
        }

        return terrainDataPoints;
    }
}
