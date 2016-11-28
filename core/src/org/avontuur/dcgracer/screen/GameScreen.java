package org.avontuur.dcgracer.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.ShortArray;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.CameraID;
import org.avontuur.dcgracer.component.Drawable;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.system.Box2dWorldSystem;
import org.avontuur.dcgracer.system.CameraEnum;
import org.avontuur.dcgracer.system.CameraUpdateSystem;
import org.avontuur.dcgracer.system.ComponentMapperSystem;
import org.avontuur.dcgracer.system.GameOverSystem;
import org.avontuur.dcgracer.system.MotionSystem;
import org.avontuur.dcgracer.system.RenderingSystem;
import org.avontuur.dcgracer.system.SpritePositionSystem;
import org.avontuur.dcgracer.utils.TerrainGenerator;
import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Created by Bram Avontuur on 2016-03-01.
 *
 * This class represents the actual game.
 */
public class GameScreen implements Screen, GestureListener, InputProcessor {

    /*
     * TODO: Deprecate all state here
    private SpriteBatch batch;
    private PolygonSpriteBatch terrainBatch;

    private ShapeRenderer shapeRenderer;

    private Sprite sprite;
    private PolygonSprite terrainSprite;
    private PolygonRegion terrainPolygonRegion;

    private Body playerBody;
    private Body bodyTerrain;

    private TrackingCamera cam;
    private TrackingCamera terrainCam;

    private Texture textureTerrainMud;

    private float[] terrain;
    private int terrain_ppm; //terrain texture pixels per meter factor
     */

    // game state variables
    private short pushDirection = 0;
    final float VIEWPORT_WIDTH = 10f;

    // Maximum frame time still acceptable for reliable physics processing
    public static final float MAX_FRAME_TIME = 1 / 15f;

    private World artemisWorld;

    public GameScreen() {
    }

    private World createWorld() {
        // Creates a World in the context of Artemis-odb's Entity Component System
        WorldConfiguration worldConfig = new WorldConfigurationBuilder()
                .with(new ComponentMapperSystem())
                .with(new Box2dWorldSystem())
                .with(new MotionSystem())
                .with(new SpritePositionSystem())
                .with(new CameraUpdateSystem(VIEWPORT_WIDTH))
                .with(new RenderingSystem())
                .with(new GameOverSystem())
                .build();

        return new World(worldConfig);
    }

    @Override
    public void show() {
        if (artemisWorld == null){
            DCGRacer.log.info("Creating The World");
            artemisWorld = createWorld();
            createPlayerEntity();
            createLandscapeEntity();
        }

    }

    @Override
    public void render(float delta) {
        // Cap delta spikes to prevent crazy world updates
        if (artemisWorld != null) {
            artemisWorld.setDelta(MathUtils.clamp(delta, 0, MAX_FRAME_TIME));
            artemisWorld.process();
        } else {
            throw new RuntimeException("World has not been created");
        }
    }

    private void createPlayerEntity() {
        Box2dWorldSystem box2dSystem = artemisWorld.getSystem(Box2dWorldSystem.class);
        CameraUpdateSystem cameraUpdateSystem = artemisWorld.getSystem(CameraUpdateSystem.class);
        ComponentMapperSystem mappers = artemisWorld.getSystem(ComponentMapperSystem.class);

        Texture gameLogo = ResourceManager.instance.gameLogo;
        Sprite sprite = new Sprite(gameLogo);
        CircleShape shape = new CircleShape();
        final float circleRadius = 0.25f;
        sprite.setSize(circleRadius * 2f, circleRadius * 2f);
        shape.setRadius(circleRadius);
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        sprite.setPosition(cam.viewportWidth / 2 - sprite.getWidth() / 2, cam.viewportHeight);
        // Setting the origin is necessary to make rotation work correctly. Default origin is at bottom left
        // corner, should be the center to match with box2d's rotation.
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // playerBody positions are at the center of the shape, sprite position at the bottom left corner. How convenient.
        bodyDef.position.set(sprite.getX() + sprite.getWidth() / 2f, sprite.getY() + sprite.getHeight() / 2f);

        Body playerBody = box2dSystem.getBox2DWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.restitution = 0.6f; // Makes it bounce
        playerBody.createFixture(fixtureDef);

        shape.dispose();

        // TODO: This looks horrible. Is this really the way to go forward?
        int e = artemisWorld.create();
        CameraID cameraID = mappers.cameraIDComponents.create(e);
        cameraID.cameraID = CameraEnum.STANDARD;
        Physics physics = mappers.physicsComponents.create(e);
        physics.body = playerBody;
        mappers.mainPlayerComponents.create(e);
        Drawable drawable = mappers.drawableComponents.create(e);
        drawable.sprite = sprite;
        // TODO: add player input, including the system
    }

    private void createLandscapeEntity() {
        Box2dWorldSystem box2dSystem = artemisWorld.getSystem(Box2dWorldSystem.class);
        CameraUpdateSystem cameraUpdateSystem = artemisWorld.getSystem(CameraUpdateSystem.class);
        ComponentMapperSystem mappers = artemisWorld.getSystem(ComponentMapperSystem.class);

        int numIterations = 10;
        float range = 18f;
        float scaleX = 0.02f;
        float scaleY = 0.25f;

        // Generate the terrain data points - this will create the vertices for a closed simply polygon
        // representing the ground terrain.
        float[] terrain;
        terrain = TerrainGenerator.generateTerrainData(numIterations, range, scaleX, scaleY);

        // Create the box2d representation of the terrain
        ChainShape terrainShape = new ChainShape();
        terrainShape.createChain(terrain);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);

        Body bodyTerrain = box2dSystem.getBox2DWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = terrainShape;
        fixtureDef.density = 1f;

        bodyTerrain.createFixture(fixtureDef);

        terrainShape.dispose();


        // Create the sprite for the terrain. The terrain is filled in using triangulation, which thankfully
        // LibGDX supplies out of the box.
        Texture textureTerrainMud = ResourceManager.instance.textureTerrainMud;
        textureTerrainMud.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textureTerrainMud.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        TextureRegion terrainRegion = new TextureRegion(textureTerrainMud);
        //terrainRegion.setRegion(0, 0, textureTerrainMud.getWidth() * 15, textureTerrainMud.getHeight() * 15);
        //terrainRegion.setU2(2);
        //terrainRegion.setV2(2);

        // This appears to need a different pixels-to-meters ratio. When repeating textures, it will use the
        // texture pixels as unit. So we need to convert those to meters. I want the ground texture to be about 1m
        // wide, so ratio is 1m = <texture width> / 2
        int terrain_ppm = textureTerrainMud.getWidth() / 2;
        // 4th from last coordinate in terrain is the right-most one (yes, this is dirty, this needs a
        // refactor of course)
        final float[] terrainVertices = transformVertices(terrain, terrain_ppm, terrain_ppm);
        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        ShortArray triangulatedShortArray = triangulator.computeTriangles(terrainVertices);
        short[] triangles = new short[triangulatedShortArray.size];
        for (int i = 0; i < triangulatedShortArray.size; i++)
            triangles[i] = triangulatedShortArray.get(i);
        PolygonRegion terrainPolygonRegion = new PolygonRegion(terrainRegion, terrainVertices, triangles);

        // Create entity
        int e = artemisWorld.create();
        CameraID cameraID = mappers.cameraIDComponents.create(e);
        cameraID.cameraID = CameraEnum.TERRAIN;
        Physics physics = mappers.physicsComponents.create(e);
        physics.body = bodyTerrain;
        Drawable drawable = mappers.drawableComponents.create(e);
        drawable.polygonRegion = terrainPolygonRegion;

        // Set camera right boundary to the width of the terrain
        // TODO: Move this to CameraUpdateSystem (or call a method there) (after the splitup into 2 systems for
        //       sprites & polygons)
        final float terrainWidth = terrain[terrain.length - 4 * 2];
        DCGRacer.log.debug("Terrain width = " + terrainWidth);
        TrackingCamera terrainCam = cameraUpdateSystem.getCamera(CameraEnum.TERRAIN);
        terrainCam.setBoundaryRight(terrainWidth);
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        cam.setBoundaryRight(terrainWidth);
    }
    /**
     * @Deprecated
    private void GameScreenDeprecated() {
        // TODO: Split me up into systems and remove me
        // INPUT HANDLING
        // --------------

        InputMultiplexer im = new InputMultiplexer();
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        im.addProcessor(this);


        Gdx.input.setInputProcessor(im);
    }
     */

    /**
     * @Deprecated
    public void renderDeprecated(float delta) {
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
     */

    @Override
    public void resize(int width, int height) {
        //TODO call resize on the cameraUpdateSystems
        CameraUpdateSystem cameraUpdateSystem = artemisWorld.getSystem(CameraUpdateSystem.class);
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        cam.resize(VIEWPORT_WIDTH, VIEWPORT_WIDTH * height / width);
        cam.update();
        TrackingCamera terrainCam = cameraUpdateSystem.getCamera(CameraEnum.TERRAIN);
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
