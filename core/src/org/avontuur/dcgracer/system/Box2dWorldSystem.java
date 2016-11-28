package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Bram Avontuur on 2016-11-27.
 *
 * System that manages the Box2D physics world and keeps it ticking
 */
public class Box2dWorldSystem extends BaseSystem {
    private static final float GRAVITY = -9.8f;

    private World box2DWorld;

    @Override
    protected void initialize() {
        super.initialize();

        box2DWorld = new World(new Vector2(0, GRAVITY), true);
    }

    @Override
    protected void processSystem() {
        // Advance the world, by the amount of time that has elapsed since the last frame
        // Generally in a real game, don't do this in the render loop, as you are tying the physics
        // update rate to the frame rate, and vice versa
        // TODO: see comment above, fine-tune these parameters.
        float gameLoopDelta = world.getDelta();
        box2DWorld.step(1f/60f, 6, 2);
        //box2DWorld.step(gameLoopDelta, 6, 2);

        // Example taken from other game (and read about this in the box2d docs somewhere):
        /*
        float frameTime = Math.min(SpasholeApp.core.getDelta(), FRAME_TIME_LIMIT);
        accumulator += frameTime;
        while (accumulator >= FIXED_SINGLE_TIME_STEP) {
            SpasholeApp.box2dWorld.step(FIXED_SINGLE_TIME_STEP, 6, 2);
            accumulator -= FIXED_SINGLE_TIME_STEP;
        }
        */
    }

    /**
     * @return Reference to the box2d world
     */
    public World getBox2DWorld() { return box2DWorld; }
}
