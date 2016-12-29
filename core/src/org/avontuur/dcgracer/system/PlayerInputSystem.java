package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Motion;
import org.avontuur.dcgracer.component.PlayerInput;

/**
 * Handles input from the user.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class PlayerInputSystem extends IteratingSystem implements GestureDetector.GestureListener, InputProcessor {
    // TODO: Move this into a superclass for all my systems
    private ComponentMapperSystem mappers;
    private Box2dWorldSystem box2dWorldSystem;

    private int pushDirection = 0;
    private boolean pause = false;
    private boolean pauseToggled = false;

    // Amount of force applied to player for each fling
    public static final float PUSH_FORCE = 10f;

    public PlayerInputSystem() {
        super(Aspect.all(PlayerInput.class, Motion.class));
    }

    @Override
    protected void process(int entityId) {
        Motion motion = mappers.motionComponents.get(entityId);

        if (pushDirection > 0) {
            motion.force.x = PUSH_FORCE;
            motion.force.y = 0;
            pushDirection = 1;
        } else if (pushDirection < 0) {
            motion.force.x = -PUSH_FORCE;
            motion.force.y = 0;
            pushDirection = -1;
        } else {
            motion.force.x = 0;
            motion.force.y = 0;
        }
    }

    @Override
    protected void end() {
        super.end();

        // TODO: Split off input processing and motion/pause handling into their own systems
        if (pauseToggled) {
            pauseToggled = false;
            box2dWorldSystem.setPause(pause);
        }
    }

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
    public boolean fling(float velocityX, float velocityY, int button) { return false; }

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
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.LEFT) {
            pushDirection = 0;
        }

        if (keycode == Input.Keys.SPACE) {
            pause = !pause;
            pauseToggled = true;
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
        if (screenX > Gdx.graphics.getWidth() / 2) {
            pushDirection = 1;
        } else {
            pushDirection = -1;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        pushDirection = 0;
        return true;
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
