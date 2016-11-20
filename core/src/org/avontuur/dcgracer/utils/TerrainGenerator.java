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
     *        Number of iterations to us to generate the terrain. More iterations give exponentially more data points.
     * @param range
     *        Upper range of the terrain data.
     * @param scaleX
     *        Multiply x coordinates by this factor; essentially moves data points closer together or further apart.
     * @param scaleY
     *        Multiple y coordinates by this factor; allows one to stretch or condense the vertical range.
     * @return
     *        Float array containing terrain data, alternating between x and y coordinates. Number of coordinates
     *        equals (2^numIterations + 1) + 3 coordinates to create a closed polygon.
     */
    public static float[] generateTerrainData(int numIterations, float range, float scaleX, float scaleY) {
        //just calculating and debug-outputting values for now
        float[] terrainDataPointsRaw = GameMath.midfieldDisplacement2D(numIterations, range);
        // +3 * 2: adding vertices to make it a closed simple polygon so it can be filled with a background texture
        float[] terrainDataPoints = new float[terrainDataPointsRaw.length * 2 + 3 * 2];

        //convert to array of alternating x,y coordinates
        for (int i = 0; i < terrainDataPointsRaw.length; i++) {
            float x = i * scaleX;
            float y = terrainDataPointsRaw[i] * scaleY;
            terrainDataPoints[i * 2] = x;
            terrainDataPoints[i * 2 + 1] = y;
        }

        // add the 3 vertices to make it a closed polygon
        int closePolygonStartIndex = terrainDataPointsRaw.length * 2;
        // Bottom-right X, then Y
        terrainDataPoints[closePolygonStartIndex] = terrainDataPoints[closePolygonStartIndex - 2];
        terrainDataPoints[closePolygonStartIndex + 1 ] = 0;
        // Bottom-left X: same as X of first coordinate
        terrainDataPoints[closePolygonStartIndex + 2] = terrainDataPoints[0];
        // Bottom-left Y
        terrainDataPoints[closePolygonStartIndex + 3] = 0;
        // Finally, close the polygon by copying the first coordinate.
        terrainDataPoints[closePolygonStartIndex + 4] = terrainDataPoints[0];
        terrainDataPoints[closePolygonStartIndex + 5] = terrainDataPoints[1];

        return terrainDataPoints;
    }
}
