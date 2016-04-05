package org.avontuur.dcgracer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ShortArray;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.manager.ScreenEnum;
import org.avontuur.dcgracer.manager.ScreenManager;
import org.avontuur.dcgracer.utils.*;

/**
 * Created by Bram Avontuur on 2016-03-01.
 *
 * This class represents the actual game.
 */
public class GameScreen implements Screen, GestureListener, InputProcessor {

    private SpriteBatch batch;
    private PolygonSpriteBatch terrainBatch;

    private ShapeRenderer shapeRenderer;

    private Sprite sprite;
    private PolygonSprite terrainSprite;
    private PolygonRegion terrainPolygonRegion;

    private World world;

    private Body playerBody;
    private Body bodyTerrain;

    private TrackingCamera cam;
    private TrackingCamera terrainCam;

    private Texture textureTerrainMud;

    private float[] terrain;
    private int terrain_ppm; //terrain texture pixels per meter factor

    // game state variables
    private short pushDirection = 0;
    final float VIEWPORT_WIDTH = 10f;

    public GameScreen() {
        cam = setupCamera(1);
        setupWorld();

        // "PLAYER"
        // --------

        Texture txtrGameLogo = new Texture(Gdx.files.internal("ball.png"));
        sprite = new Sprite(txtrGameLogo);
        CircleShape shape = new CircleShape();
        final float circleRadius = 0.25f;
        sprite.setSize(circleRadius * 2f, circleRadius * 2f);
        shape.setRadius(circleRadius);
        sprite.setPosition(cam.viewportWidth / 2 - sprite.getWidth() / 2, cam.viewportHeight);
        // Setting the origin is necessary to make rotation work correctly. Default origin is at bottom left
        // corner, should be the center to match with box2d's rotation.
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // playerBody positions are at the center of the shape, sprite position at the bottom left corner. How convenient.
        bodyDef.position.set(sprite.getX() + sprite.getWidth() / 2f, sprite.getY() + sprite.getHeight() / 2f);

        playerBody = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.restitution = 0.6f; // Makes it bounce
        playerBody.createFixture(fixtureDef);

        shape.dispose();

        // GROUND
        // ------

        int numIterations = 10;
        float range = 18f;
        float scaleX = 0.02f;
        float scaleY = 0.25f;

        // Generate the terrain data points - this will create the vertices for a closed simply polygon
        // representing the ground terrain.
        terrain = generateTerrainData(numIterations, range, scaleX, scaleY);

        // Create the box2d representation of the terrain
        ChainShape terrainShape = new ChainShape();
        terrainShape.createChain(terrain);

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);

        bodyTerrain = world.createBody(bodyDef);

        fixtureDef = new FixtureDef();
        fixtureDef.shape = terrainShape;
        fixtureDef.density = 1f;

        bodyTerrain.createFixture(fixtureDef);

        terrainShape.dispose();


        // Create the sprite for the terrain. The terrain is filled in using triangulation, which thankfully
        // LibGDX supplies out of the box.
        textureTerrainMud = new Texture(Gdx.files.internal("background_mud.png"));
        textureTerrainMud.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textureTerrainMud.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion terrainRegion = new TextureRegion(textureTerrainMud);
        //terrainRegion.setRegion(0, 0, textureTerrainMud.getWidth() * 15, textureTerrainMud.getHeight() * 15);
        //terrainRegion.setU2(2);
        //terrainRegion.setV2(2);

        // This appears to need a different pixels-to-meters ratio. When repeating textures, it will use the
        // texture pixels as unit. So we need to convert those to meters. I want the ground texture to be about 1m
        // wide, so ratio is 1m = <texture width> / 2
        terrain_ppm = textureTerrainMud.getWidth() / 2;
        // 4th from last coordinate in terrain is the right-most one (yes, this is dirty, this needs a
        // refactor of course)
        final float[] terrainVertices = transformVertices(terrain, terrain_ppm, terrain_ppm);
        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        ShortArray triangulatedShortArray = triangulator.computeTriangles(terrainVertices);
        short[] triangles = new short[triangulatedShortArray.size];
        for (int i = 0; i < triangulatedShortArray.size; i++)
            triangles[i] = triangulatedShortArray.get(i);
        terrainPolygonRegion = new PolygonRegion(terrainRegion, terrainVertices, triangles);
        //terrainSprite = new PolygonSprite(terrainPolygonRegion);
        //DCGRacer.log.debug("Terrain sprite size: " + terrainSprite.getWidth() + ", " + terrainSprite.getHeight());

        // Set camera right boundary to the width of the terrain
        final float terrainWidth = terrain[terrain.length - 4 * 2];
        DCGRacer.log.debug("Terrain width = " + terrainWidth);
        terrainCam = setupCamera(terrain_ppm);
        terrainCam.setBoundaryRight(terrainWidth);
        cam.setBoundaryRight(terrainWidth);


        // INPUT HANDLING
        // --------------

        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        im.addProcessor(this);


        Gdx.input.setInputProcessor(im);

        // OTHER
        // -----

        batch = new SpriteBatch();
        terrainBatch = new PolygonSpriteBatch();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        cam.center(playerBody, delta);
        cam.update();
        terrainCam.center(playerBody, delta);
        terrainCam.update();

        // TODO: force applied has a greatly different impact on desktop vs android. Why? Render speed I guess?
        final float pushForce = 15f;
        if (pushDirection > 0) {
            playerBody.applyForceToCenter(pushForce, 0f, true);
        } else if (pushDirection < 0) {
            playerBody.applyForceToCenter(-pushForce, 0f, true);
        }
        pushDirection = 0; //reset after each event!

        batch.setProjectionMatrix(cam.combined);

        // Advance the world, by the amount of time that has elapsed since the last frame
        // Generally in a real game, dont do this in the render loop, as you are tying the physics
        // update rate to the frame rate, and vice versa
        // TODO: see comment above
        world.step(1f/60f, 6, 2);

        // Now update the sprite position accordingly to its now updated Physics playerBody
        // TODO: add wrapper class that manages both sprite and playerBody?
        sprite.setPosition(playerBody.getPosition().x - sprite.getWidth() / 2, playerBody.getPosition().y - sprite.getHeight() / 2);
        sprite.setRotation(playerBody.getAngle() * MathUtils.radiansToDegrees);
        //DCGRacer.log.debug("sprite size = " + sprite.getWidth() + "," + sprite.getHeight());
        //DCGRacer.log.debug("sprite position = " + sprite.getX() + "," + sprite.getY());

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        //batch.draw(sprite, sprite.getX(), sprite.getY());
        sprite.draw(batch);
        batch.end();
        renderTerrain();

        if (playerBody.getPosition().y < 0) {
            ScreenManager.getInstance().showScreen(ScreenEnum.GAMEOVER);
        }

    }

    @Override
    public void resize(int width, int height) {
        cam.resize(VIEWPORT_WIDTH, VIEWPORT_WIDTH * height / width);
        cam.update();
        terrainCam.resize(VIEWPORT_WIDTH, VIEWPORT_WIDTH * height / width);
        terrainCam.update();
        DCGRacer.log.debug("Resized to " + cam.viewportWidth + "," + cam.viewportHeight);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        sprite.getTexture().dispose();
        textureTerrainMud.dispose();
        world.dispose();
        batch.dispose();
        terrainBatch.dispose();
        shapeRenderer.dispose();
    }

    private TrackingCamera setupCamera(final float units_per_meter) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Constructs a new TrackingCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio. The Camera's units match the Physics' world
        // units. This requires that all sprites have their world size set explicitly, so
        // that we never have to worry about pixels anymore.
        float lerp = 0.95f;
        TrackingCamera cam = new TrackingCamera(VIEWPORT_WIDTH, VIEWPORT_WIDTH * (h / w),
                units_per_meter, lerp, true, false);
        cam.setBoundaryLeft(0); //don't pan past the left side of the world

        // set camera in bottom-left corner. Position is in the center of the viewport, so adjust
        // accordingly.
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        return cam;
    }

    private void setupWorld() {
        world = new World(new Vector2(0, -9.8f), true);
    }

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
    private float[] generateTerrainData(int numIterations, float range, float scaleX, float scaleY) {
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

    private void renderTerrainDebug(float[] terrain) {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 1);
        // terrain is always of even length
        for (int i = 0; i < terrain.length - 2; i += 2) {
            shapeRenderer.line(terrain[i], terrain[i + 1], terrain[i + 2], terrain[i + 3]);
        }
        shapeRenderer.end();
    }

    private void renderTerrain() {
        terrainBatch.setProjectionMatrix(terrainCam.combined);
        terrainBatch.begin();
        terrainBatch.draw(terrainPolygonRegion, 0, 0);
        terrainBatch.end();
    }
    // INPUT HANDLING

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (velocityX > 0) {
            pushDirection = 1;
            return true;
        } else if (velocityX < 0) {
            pushDirection = -1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.RIGHT) {
            pushDirection = 1;
            return true;
        } else if (keycode == Input.Keys.LEFT) {
            pushDirection = -1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
