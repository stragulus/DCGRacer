package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.ShortArray;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.utils.TerrainGenerator;
import org.avontuur.dcgracer.utils.TrackingCamera;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * The terrain is constructed of vertical segments that are created as soon as they become visible. This system
 * tracks which segments are needed, and will add & remove segments.
 *
 * Created by Bram Avontuur on 2016-11-30.
 */

public class LandscapeUpdateSystem extends BaseSystem {
    private ComponentMapperSystem mappers;
    private CameraUpdateSystem cameraUpdateSystem;
    private Box2dWorldSystem box2dWorldSystem;

    // Number of indices in array terrainDataPoints used for each datapoint.
    private static final short INDICES_PER_DATAPOINT = 2;
    // terrain data points - a list of coordinates expressed as sequential x and y coordinates,
    // e.g. [x0, y0, x1, y1, ..]
    private float[] terrainDataPoints;
    // entities currently displayed.
    private LinkedList<Integer> entitiesOnScreen;
    private TextureRegion terrainRegion;
    // number of pixels per world-unit (meters)
    private int terrainPPM;
    // width of viewport, in world units (meters)
    private float viewPortWidth;
    // start index, in terrainDataPoints, for next terrain entity to be created
    private int nextTerrainDataPointsIndex = 0;
    // number of data points to use for individual sliced terrain segments
    private int terrainDataPointsPerSegment;

    public LandscapeUpdateSystem(final float viewportWidth) {
        this.viewPortWidth = viewportWidth;

        // Generate the terrain data points - this will create the vertices for a closed simple polygon
        // representing the ground terrain.

        // When this actually works, levels will be pre-generated instead of procedurally generated at game start.
        // For now, use some hardcoded values.
        int numIterations = 11;
        float range = 18f;
        float scaleX = 0.04f; //distance between 2 points
        float scaleY = 0.25f;

        this.terrainDataPoints = TerrainGenerator.generateTerrainData(numIterations, range, scaleX, scaleY);
        // Each segment should take up about 1/4th of the screen
        this.terrainDataPointsPerSegment = (int)(this.viewPortWidth / 4f / scaleX);
        this.entitiesOnScreen = new LinkedList<Integer>();

        Texture textureTerrainMud = ResourceManager.instance.textureTerrainMud;
        textureTerrainMud.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textureTerrainMud.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.terrainRegion = new TextureRegion(textureTerrainMud);

        // This appears to need a different pixels-to-meters ratio. When repeating textures, it will use the
        // texture pixels as unit. So we need to convert those to meters. I want the ground texture to be about 1m
        // wide, so ratio is 1m = <texture width> / 2
        this.terrainPPM = textureTerrainMud.getWidth() / 2;
    }

    @Override
    protected void processSystem() {
        // Get the x coordinate for the right boundary of the main camera on the world
        float cameraRightBoundary = getCameraRightBoundary();
        // get the right bound of the right-most terrain entity that has already been added to the world
        float terrainRightBoundary = getTerrainRightBoundary();
        float viewPortWidthTenPct = this.viewPortWidth * 0.1f;
        // While the terrain boundary does not exceed the camera boundary by at least 10% of the world's viewport
        // width, keep adding new terrain
        while (!endOfTerrain() && (terrainRightBoundary < cameraRightBoundary + viewPortWidthTenPct)) {
            addTerrainRight();
            terrainRightBoundary = getTerrainRightBoundary();
        }
        removeTerrainLeft();

        if (endOfTerrain()) {
            // Prevent the cameras from going beyond the end of the world.
            DCGRacer.log.debug("endOfTerrain detected - setting camera bound");
            float terrainWidth = this.terrainDataPoints[this.terrainDataPoints.length - INDICES_PER_DATAPOINT];
            TrackingCamera terrainCam = cameraUpdateSystem.getCamera(CameraEnum.TERRAIN);
            terrainCam.setBoundaryRight(terrainWidth);
            TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
            cam.setBoundaryRight(terrainWidth);

        }
    }

    private float getCameraRightBoundary() {
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        return cam.position.x + cam.viewportWidth / 2;
    }

    private float getTerrainRightBoundary() {
        // start of the next to be added terrain segment == end of last added terrain segment
        return this.terrainDataPoints[this.nextTerrainDataPointsIndex];
    }
    private boolean endOfTerrain() {
        // If the index for the next created terrain segment is the last element in the data points array,
        // then nothing else needs to be added.
        return this.nextTerrainDataPointsIndex == this.terrainDataPoints.length - INDICES_PER_DATAPOINT;
    }

    private void addTerrainRight() {
        int terrainIndexFrom = this.nextTerrainDataPointsIndex;
        DCGRacer.log.debug("addTerrainRight([" + terrainIndexFrom + "]->("
                + this.terrainDataPoints[terrainIndexFrom] + ", "
                + this.terrainDataPoints[terrainIndexFrom + 1] + "))");

        // terrainIndexTo is exclusive; the index itself will not be part of the new terrain element
        // Note that each coordinate in terrainDataPoints uses 2 indices
        int terrainIndexTo = terrainIndexFrom + this.terrainDataPointsPerSegment * INDICES_PER_DATAPOINT;

        if (terrainIndexTo > this.terrainDataPoints.length) {
            terrainIndexTo = this.terrainDataPoints.length;
        }

        // starting index of the next segments (inclusive!) equals the data point of its preceding segment.
        // Again, two indices per data point
        this.nextTerrainDataPointsIndex = terrainIndexTo - INDICES_PER_DATAPOINT;

        // slice the terrainDataPoints array to the section required for the new segment; reserve extra space for
        // 3 coordinates to close the polygon.
        float[] segmentTerrainDataPoints = new float[terrainIndexTo - terrainIndexFrom + 3 * INDICES_PER_DATAPOINT];
        System.arraycopy(terrainDataPoints, terrainIndexFrom, segmentTerrainDataPoints, 0,
                terrainIndexTo - terrainIndexFrom);
        closePolygon(segmentTerrainDataPoints);

        // Create entity
        int e = getWorld().create();
        Physics physics = mappers.physicsComponents.create(e);
        physics.body = createTerrainBody(segmentTerrainDataPoints);
        org.avontuur.dcgracer.component.PolygonRegion polygonRegionComponent =
                mappers.polygonRegionComponents.create(e);
        polygonRegionComponent.polygonRegion = createTerrainPolygonRegion(segmentTerrainDataPoints);
        // Add to internal linked list of entities so that they can be easily removed later.
        this.entitiesOnScreen.push(e);
    }

    private void closePolygon(float[] segments) {
        //this assumes the last 3 data points in the segments array are to be populated with the polygon-closing
        //coordinates, and that there are at least 2 data points in segments.
        int closePolygonStartIndex = segments.length - INDICES_PER_DATAPOINT * 3;

        // add the 3 vertices to make it a closed polygon
        // Bottom-right X&Y; get X from previous coordinate, Y=0
        segments[closePolygonStartIndex] = segments[closePolygonStartIndex - 2];
        segments[closePolygonStartIndex + 1 ] = 0;
        // Bottom-left X&Y: X=same as X of first coordinate, Y=0 again
        segments[closePolygonStartIndex + 2] = segments[0];
        segments[closePolygonStartIndex + 3] = 0;
        // Finally, close the polygon by copying the first coordinate.
        segments[closePolygonStartIndex + 4] = segments[0];
        segments[closePolygonStartIndex + 5] = segments[1];

    }
    private void removeTerrainLeft() {
        //TODO: implement me!
    }

    private PolygonRegion createTerrainPolygonRegion(float[] segmentTerrainDataPoints) {
        final float[] terrainVertices = transformVertices(segmentTerrainDataPoints, terrainPPM, terrainPPM);
        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        ShortArray triangulatedShortArray = triangulator.computeTriangles(terrainVertices);
        short[] triangles = new short[triangulatedShortArray.size];

        for (int i = 0; i < triangulatedShortArray.size; i++)
            triangles[i] = triangulatedShortArray.get(i);

        PolygonRegion terrainPolygonRegion = new PolygonRegion(terrainRegion, terrainVertices, triangles);
        return terrainPolygonRegion;
    }

    // create a Box2D Physics Body from a list of coordinates representing a polygon
    private Body createTerrainBody(float[] segmentTerrainDataPoints) {
        // Create the box2d representation of the terrain and the triangulated texture.
        ChainShape terrainShape = new ChainShape();
        terrainShape.createChain(segmentTerrainDataPoints);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);

        Body bodyTerrain = box2dWorldSystem.getBox2DWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = terrainShape;
        fixtureDef.density = 1f;

        bodyTerrain.createFixture(fixtureDef);
        terrainShape.dispose();
        return bodyTerrain;
    }


    // map world unit vertices to pixel-based vertices
    private float[] transformVertices(float[] vertices, float scaleX, float scaleY) {
        // This assumes each vertex' coordinate occupies 2 sequential indexes in vertices
        float[] result = new float[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            if (i % 2 == 0) {
                result[i] = vertices[i] * scaleX;
            } else {
                result[i] = vertices[i] * scaleY;
            }
        }

        return result;
    }

}
