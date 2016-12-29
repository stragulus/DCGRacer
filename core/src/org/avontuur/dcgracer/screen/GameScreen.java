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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.system.Box2dWorldSystem;
import org.avontuur.dcgracer.system.CameraEnum;
import org.avontuur.dcgracer.system.CameraUpdateSystem;
import org.avontuur.dcgracer.system.ComponentMapperSystem;
import org.avontuur.dcgracer.system.DebugRenderingSystem;
import org.avontuur.dcgracer.system.GameOverSystem;
import org.avontuur.dcgracer.system.HUDDisplaySystem;
import org.avontuur.dcgracer.system.LandscapeUpdateSystem;
import org.avontuur.dcgracer.system.MotionSystem;
import org.avontuur.dcgracer.system.PlayerInputSystem;
import org.avontuur.dcgracer.system.PolygonRegionRenderingSystem;
import org.avontuur.dcgracer.system.RenderCanvasSystem;
import org.avontuur.dcgracer.system.SpritePositionSystem;
import org.avontuur.dcgracer.system.SpriteRenderingSystem;
import org.avontuur.dcgracer.utils.BodyEditorLoader;
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
                //.with(new DebugRenderingSystem())
                .with(new PolygonRegionRenderingSystem())
                .with(new HUDDisplaySystem())
                .with(new GameOverSystem())
                .build();

        return new World(worldConfig);
    }

    @Override
    public void show() {
        if (artemisWorld == null){
            DCGRacer.log.info("Creating The World");
            artemisWorld = createWorld();
            DCGRacer.log.debug("Creating player & car");
            createPlayerEntity();
            createCarBodyEntity();
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

    private void createCarBodyEntity() {
        // PoC for creating a car body from a sprite and a polygon.
        Box2dWorldSystem box2dSystem = artemisWorld.getSystem(Box2dWorldSystem.class);
        ComponentMapperSystem mappers = artemisWorld.getSystem(ComponentMapperSystem.class);
        CameraUpdateSystem cameraUpdateSystem = artemisWorld.getSystem(CameraUpdateSystem.class);

        // Polygon data created using PhysicsBodyEditor; what an awesome free tool!
        BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/car_body_polygon.json"));

        BodyDef dynamicBodyDef = new BodyDef();
        dynamicBodyDef.type = BodyDef.BodyType.DynamicBody;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0f;

        Texture carBodyTexture = ResourceManager.instance.carBody;
        float carWidth = 3.5f;
        float carHeight = carBodyTexture.getHeight() / (carBodyTexture.getWidth() / carWidth);

        Sprite carBodySprite = new Sprite(carBodyTexture);
        carBodySprite.setSize(carWidth, carHeight);
        // sprite Origin must match body origin; body origin is defined to be (0,0) in the json data file, generated
        // by the Physics Body Editor.
        carBodySprite.setOrigin(0, 0);
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        // TODO: Sprite position set here does not reflect actual sprite position in the game; probably because resize()
        //       has never been called..?
        carBodySprite.setPosition(cam.viewportWidth * 0.2f - carBodySprite.getWidth() / 2, cam.viewportHeight * 1.1f);
        DCGRacer.log.debug("Car body " + carBodySprite + " position = " + carBodySprite.getX() + ", " + carBodySprite.getY());
        DCGRacer.log.debug("Car body size = " + carBodySprite.getWidth() + ", " + carBodySprite.getHeight());
        dynamicBodyDef.position.set(carBodySprite.getX() + carBodySprite.getWidth() / 2f,
                carBodySprite.getY() + carBodySprite.getHeight() / 2f);

        Body carBody = box2dSystem.getBox2DWorld().createBody(dynamicBodyDef);
        // adds a PolygonShape to the body
        loader.attachFixture(carBody, "Car Body", fixtureDef, carWidth);

        int e = artemisWorld.create();
        Physics physics = mappers.physicsComponents.create(e);
        physics.body = carBody;
        org.avontuur.dcgracer.component.Sprite spriteComponent = mappers.spriteComponents.create(e);
        spriteComponent.sprite = carBodySprite;
        // this will make the camera track the car body
        mappers.mainPlayerComponents.create(e);

        // Right Wheel
        // ***********

        CircleShape wheelShape = new CircleShape();
        float wheelRadius = 0.4f;

        wheelShape.setRadius(wheelRadius);
        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 0.5f;
        wheelFixtureDef.restitution = 0.2f;
        wheelFixtureDef.friction = 1f;
        wheelFixtureDef.shape = wheelShape;
        Body rightWheelBody = box2dSystem.getBox2DWorld().createBody(dynamicBodyDef);
        rightWheelBody.createFixture(wheelFixtureDef);
        WheelJointDef wheelJointDef = new WheelJointDef();
        wheelJointDef.bodyA = carBody;
        wheelJointDef.bodyB = rightWheelBody;
        /// localAnchorA is relative to bodyA's origin
        // float wheelBodyAnchorY = -carBodySprite.getHeight() / 2;
        float wheelBodyAnchorY = 0;
        //wheelJointDef.localAnchorA.set(2.65f, wheelBodyAnchorY);
        wheelJointDef.localAnchorA.set(2.65f, 0);
        //wheelJointDef.localAnchorB.set(0, 0); // circle origin is center...right?
        // move along which axis (I guess)
        wheelJointDef.localAxisA.set(Vector2.Y); // along which axis the wheel can move
        wheelJointDef.frequencyHz = 10; // wut diz ??
        wheelJointDef.dampingRatio = 0.2f;
        wheelJointDef.maxMotorTorque = fixtureDef.density * 10; // copypasta from some example
        WheelJoint rightWheelJoint = (WheelJoint)box2dSystem.getBox2DWorld().createJoint(wheelJointDef);

        Texture wheelTexture = ResourceManager.instance.wheel;
        Sprite rightWheelSprite = new Sprite(wheelTexture);
        rightWheelSprite.setSize(wheelRadius * 2f, wheelRadius * 2f);
        rightWheelSprite.setOrigin(rightWheelSprite.getWidth() / 2, rightWheelSprite.getHeight() / 2);

        // not setting position; SpritePositioningSystem will take care of that for us!
        e = artemisWorld.create();
        physics = mappers.physicsComponents.create(e);
        physics.body = rightWheelBody;
        spriteComponent = mappers.spriteComponents.create(e);
        spriteComponent.sprite = rightWheelSprite;
        org.avontuur.dcgracer.component.WheelJoint rightWheelJointComponent = mappers.wheelJointComponents.create(e);
        rightWheelJointComponent.wheelJoint = rightWheelJoint;

        // Left Wheel
        // **********

        Body leftWheelBody = box2dSystem.getBox2DWorld().createBody(dynamicBodyDef);
        leftWheelBody.createFixture(wheelFixtureDef);
        wheelJointDef.bodyB = leftWheelBody;
        wheelJointDef.localAnchorA.set(0.8f, wheelBodyAnchorY);

        WheelJoint leftWheelJoint = (WheelJoint)box2dSystem.getBox2DWorld().createJoint(wheelJointDef);

        Sprite leftWheelSprite = new Sprite(wheelTexture);
        leftWheelSprite.setSize(wheelRadius * 2f, wheelRadius * 2f);
        leftWheelSprite.setOrigin(rightWheelSprite.getWidth() / 2, rightWheelSprite.getHeight() / 2);

        e = artemisWorld.create();
        physics = mappers.physicsComponents.create(e);
        physics.body = leftWheelBody;
        spriteComponent = mappers.spriteComponents.create(e);
        spriteComponent.sprite = leftWheelSprite;
        org.avontuur.dcgracer.component.WheelJoint leftWheelJointComponent = mappers.wheelJointComponents.create(e);
        leftWheelJointComponent.wheelJoint = leftWheelJoint;

        // let the player can control this wheel - rear wheel drive!
        mappers.playerInputComponents.create(e);
        mappers.motionComponents.create(e);
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
        //mappers.mainPlayerComponents.create(e);
        //mappers.motionComponents.create(e);
        mappers.playerInputComponents.create(e);
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

        // Also update the HUD
        HUDDisplaySystem hudDisplaySystem = artemisWorld.getSystem(HUDDisplaySystem.class);
        hudDisplaySystem.resize(width, height);

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
        HUDDisplaySystem hudDisplaySystem = artemisWorld.getSystem(HUDDisplaySystem.class);
        hudDisplaySystem.dispose();
    }
}
