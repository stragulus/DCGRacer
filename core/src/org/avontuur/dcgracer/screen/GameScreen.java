package org.avontuur.dcgracer.screen;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.system.Box2dWorldSystem;
import org.avontuur.dcgracer.system.CameraEnum;
import org.avontuur.dcgracer.system.CameraUpdateSystem;
import org.avontuur.dcgracer.system.ComponentMapperSystem;
import org.avontuur.dcgracer.system.GameOverSystem;
import org.avontuur.dcgracer.system.LandscapeUpdateSystem;
import org.avontuur.dcgracer.system.MotionSystem;
import org.avontuur.dcgracer.system.PlayerInputSystem;
import org.avontuur.dcgracer.system.PolygonRegionRenderingSystem;
import org.avontuur.dcgracer.system.RenderCanvasSystem;
import org.avontuur.dcgracer.system.SpritePositionSystem;
import org.avontuur.dcgracer.system.SpriteRenderingSystem;
import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Created by Bram Avontuur on 2016-03-01.
 *
 * This class represents the actual game.
 */
public class GameScreen implements Screen {

    // with, in world units (meters) of the visible world
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
                .with(new PlayerInputSystem())
                .with(new MotionSystem())
                .with(new SpritePositionSystem())
                .with(new CameraUpdateSystem(VIEWPORT_WIDTH))
                .with(new LandscapeUpdateSystem(VIEWPORT_WIDTH))
                .with(new RenderCanvasSystem())
                .with(new SpriteRenderingSystem())
                .with(new PolygonRegionRenderingSystem())
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
            setupInput();
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

    private void setupInput() {
        PlayerInputSystem inputSystem = artemisWorld.getSystem(PlayerInputSystem.class);
        InputMultiplexer im = new InputMultiplexer();
        im.addProcessor(new GestureDetector(inputSystem));
        im.addProcessor(inputSystem);
        Gdx.input.setInputProcessor(im);
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
        mappers.mainPlayerComponents.create(e);
        mappers.playerInputComponents.create(e);
        mappers.motionComponents.create(e);
        Physics physics = mappers.physicsComponents.create(e);
        physics.body = playerBody;
        org.avontuur.dcgracer.component.Sprite spriteComponent = mappers.spriteComponents.create(e);
        spriteComponent.sprite = sprite;
    }

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
}
