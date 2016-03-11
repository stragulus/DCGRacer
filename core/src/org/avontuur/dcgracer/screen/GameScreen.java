package org.avontuur.dcgracer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.utils.*;

/**
 * Created by Bram Avontuur on 2016-03-01.
 *
 * This class represents the actual game.
 */
public class GameScreen implements Screen {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Sprite sprite;
    private World world;
    private Body body;
    private Body bodyTerrain;
    private OrthographicCamera cam;
    private float[] terrain;

    final float PIXELS_TO_METERS = 100f;
    //final float WORLD_WIDTH = 30; // meters
    final float WORLD_HEIGHT = 10f;
    final float VIEWPORT_WIDTH = 30f;

    public GameScreen() {
        setupCamera();
        setupWorld();

        // "PLAYER"
        Texture txtrGameLogo = new Texture(Gdx.files.internal("badlogic.jpg"));
        sprite = new Sprite(txtrGameLogo);
        DCGRacer.log.debug("sprite size = " + sprite.getWidth() + "," + sprite.getHeight());
        // Center the sprite in the top/middle of the screen
        sprite.setSize(2f, 2f); //meters
        sprite.setPosition(cam.viewportWidth / 2 - sprite.getWidth() / 2, cam.viewportHeight / 2);
        // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // body positions are at the center of the shape, sprit position at the bottom left corner. How convenient.
        bodyDef.position.set(sprite.getX() + sprite.getWidth() / 2f, sprite.getY() + sprite.getHeight() / 2f);

        // Create a body in the world using our definition
        body = world.createBody(bodyDef);

        // Now define the dimensions of the physics shape
        //PolygonShape shape = new PolygonShape();
        //shape.setAsBox(sprite.getWidth()/2, sprite.getHeight()/2);
        // let's fake a circle here for more fun terrain action, I know the sprite is square..
        CircleShape shape = new CircleShape();
        shape.setRadius(1f);

        // FixtureDef is a confusing expression for physical properties
        // Basically this is where you, in addition to defining the shape of the body
        // you also define it's properties like density, restitution and others we will see shortly
        // If you are wondering, density and area are used to calculate over all mass
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.restitution = 0.6f; //make it bounce
        body.createFixture(fixtureDef);

        shape.dispose();

        //GROUND
        int numIterations = 9;
        float range = 10f;
        float scaleX = 0.0625f;
        float scaleY = 1f;

        terrain = generateTerrain(numIterations, range, scaleX, scaleY);

        ChainShape terrainShape = new ChainShape();
        terrainShape.createChain(terrain);

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);

        Body groundBody = world.createBody(bodyDef);

        fixtureDef = new FixtureDef();
        fixtureDef.shape = terrainShape;
        fixtureDef.density = 1f;

        groundBody.createFixture(fixtureDef);

        terrainShape.dispose();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        cam.update();
        batch.setProjectionMatrix(cam.combined);

        // Advance the world, by the amount of time that has elapsed since the last frame
        // Generally in a real game, dont do this in the render loop, as you are tying the physics
        // update rate to the frame rate, and vice versa
        // TODO: see comment above
        world.step(Gdx.graphics.getDeltaTime(), 6, 2);

        // Now update the sprite position accordingly to its now updated Physics body
        // TODO: add wrapper class that manages both sprite and body?
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        //DCGRacer.log.debug("sprite size = " + sprite.getWidth() + "," + sprite.getHeight());
        //DCGRacer.log.debug("sprite position = " + sprite.getX() + "," + sprite.getY());

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        //batch.draw(sprite, sprite.getX(), sprite.getY());
        sprite.draw(batch);
        batch.end();
        renderTerrain(terrain);

    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = VIEWPORT_WIDTH;
        cam.viewportHeight = VIEWPORT_WIDTH * height / width;
        cam.update();
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
        world.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }

    private void setupCamera() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Constructs a new OrthographicCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio. The Camera's units match the Physics' world
        // units. This requires that all sprites have their world size set explicitly, so
        // that we never have to worry about pixels anymore.
        cam = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_WIDTH * (h / w));

        // set camera in bottom-left corner. Position is in the center of the viewport, so adjust
        // accordingly.
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
    }

    private void setupWorld() {
        world = new World(new Vector2(0, -9.8f), true);
    }

    private float[] generateTerrain(int numIterations, float range, float scaleX, float scaleY) {
        //just calculating and debug-outputting values for now
        float[] terrainDataPointsRaw = GameMath.midfieldDisplacement2D(numIterations, range);
        float[] terrainDataPoints = new float[terrainDataPointsRaw.length * 2];

        //convert to array of alternating x,y coordinates
        for (int i = 0; i < terrainDataPointsRaw.length; i++) {
            float x = i * scaleX;
            float y = terrainDataPointsRaw[i] * scaleY;
            terrainDataPoints[i * 2] = x;
            terrainDataPoints[i * 2 + 1] = y;
        }
        //for (int i= 0; i < terrainDataPoints.length; i++) {
        //    DCGRacer.log.debug("Terrain[" + i + "] = " + terrainDataPoints[i]);
        //}
        return terrainDataPoints;
    }

    private void renderTerrain(float[] terrain) {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 1);
        // terrain is always of even length
        for (int i = 0; i < terrain.length - 2; i += 2) {
            shapeRenderer.line(terrain[i], terrain[i + 1], terrain[i + 2], terrain[i + 3]);
        }
        shapeRenderer.end();
    }
}
