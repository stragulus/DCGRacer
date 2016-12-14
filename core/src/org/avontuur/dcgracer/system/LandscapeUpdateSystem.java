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
    private boolean rightBoundarySet = false;

    public LandscapeUpdateSystem(final float viewportWidth) {
        this.viewPortWidth = viewportWidth;

        // Generate the terrain data points - this will create the vertices for a closed simple polygon
        // representing the ground terrain.
        this.terrainDataPoints = getTerrainData();
        // Each segment should take up about 1/4th of the screen
        // this.terrainDataPointsPerSegment = (int)(this.viewPortWidth / 16f / scaleX);
        // Just put a cap on the maximum datapoints per segment; higher means bigger physics objects and less total
        // segments
        this.terrainDataPointsPerSegment = 100;
        // TODO: Looks like the segment overlaps are creating undesired physics effects, such as the ball suddenly
        //       bouncing. May want to switch the physics surface to use contiguous EdgeShapes which are swapped out?
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

        if (endOfTerrain() && !this.rightBoundarySet) {
            // Prevent the cameras from going beyond the end of the world.
            DCGRacer.log.debug("endOfTerrain detected - setting camera bound");
            this.rightBoundarySet = true;
            float terrainWidth = this.terrainDataPoints[this.terrainDataPoints.length - INDICES_PER_DATAPOINT];
            TrackingCamera terrainCam = cameraUpdateSystem.getCamera(CameraEnum.TERRAIN);
            terrainCam.setBoundaryRight(terrainWidth);
            TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
            cam.setBoundaryRight(terrainWidth);
        }
    }

    private float[] getTerrainData() {
        // When this actually works, levels will be pre-generated instead of procedurally generated at game start.
        // For now, use some hardcoded values.
        float[] result;

        // very bumpy terrain - does not perform well!
        //result = generateTerrainDataProcedurally(11, 18f, 0.02f, 0.25f, 0.5f, 80);
        // way smoother but very straight edges.
        //result = generateTerrainDataProcedurally(11, 18f, 0.2f, 0.25f, 0.5f, 8);
        // bumpy without too many datapoints
        // result = generateTerrainDataProcedurally(8, 18f, 0.2f, 0.25f, 0.5f, 80);
        // bumpy with long straight edges. This stitching together is still stupid; between segments, the
        // transition is often not very smooth
        //result = generateTerrainDataProcedurally(5, 18f, 2f, 0.25f, 0.5f, 800);
        // Just 1 segment, with roughness set to a higher value for that extra bumpiness.
        result = generateTerrainDataProcedurally(11, 18f, 2f, 0.25f, 0.91f, 1);

        return result;
    }

    private float[] generateTerrainDataProcedurally(final int numIterations, final float range, final float scaleX,
                                                    final float scaleY, final float roughness,
                                                    final int numTerrainSets) {
        float xOffset = 0; //X Coordinate to start with for a new terrain set
        float yOffset = range / 2 * scaleY; //Y coordinate to start with for a new terrain set
        int terrainDataPointsOffset = 0; // index in terrainDataPointsOffset to start appending to

        DCGRacer.log.debug("Generating terrain with scaleX=" + scaleX);
        // Instead of generating 1 terrain data set with a higher number of iterations, generate multiple sets
        // with a lower iteration count. The reason for this is that more iterations will smooth out the terrain
        // more. The areas overlap because they all start at the same Y-coordinate.
        // TODO: fugly, don't want these weird bumps in between sets, but for now it will do
        float[] allDataPoints = null;

        for (int i =0; i < numTerrainSets; i++) {
            float[] dataPoints = TerrainGenerator.generateTerrainData(numIterations, range, scaleX, scaleY, xOffset,
                    yOffset, roughness);

            if (allDataPoints == null) {
                allDataPoints = new float[dataPoints.length * numTerrainSets];
            }
            System.arraycopy(dataPoints, 0, allDataPoints, terrainDataPointsOffset, dataPoints.length);
            terrainDataPointsOffset += dataPoints.length;

            xOffset = dataPoints[dataPoints.length - 2] + scaleX;
            yOffset = dataPoints[dataPoints.length - 1]; // little stupid, this makes little horizontal paths.
        }

        return allDataPoints;
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
        DCGRacer.log.debug("addTerrainRight!!([" + terrainIndexFrom + "]->("
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
        System.arraycopy(this.terrainDataPoints, terrainIndexFrom, segmentTerrainDataPoints, 0,
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
