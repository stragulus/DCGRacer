package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.component.CarEngine;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;

/**
 * System to generate car engine sound for cars.
 *
 * Created by Bram Avontuur on 2016-12-30.
 */

public class CarEngineSoundSystem extends IteratingSystem {
    // Pitch when engine is idling
    private static final float IDLE_PITCH = 1;
    // Maximum pitch
    private static final float MAX_PITCH = 9;

    // Time it takes, in seconds, to go from idle pitch to maximum speed pitch; maximum speed is maximum speed of car
    // with all upgrades (once implemented)
    private static final float PITCH_FULL_RANGE_TIMESPAN = 0.5f;

    private ComponentMapperSystem mappers;

    public CarEngineSoundSystem() {
        super(Aspect.all(CarEngine.class, Physics.class));
    }

    @Override
    protected void process(int entityId) {
        CarEngine carEngineComponent = mappers.carEngineComponents.get(entityId);
        Body body = mappers.physicsComponents.get(entityId).body;

        if (carEngineComponent.carEngineSoundId == -1) {
            carEngineComponent.carEngineSoundId = ResourceManager.instance.carEngineSound.loop();
        }

        float maxSpeed = 50; // hardcoded for now
        float speed = Math.min(Math.abs(body.getLinearVelocity().x), maxSpeed);
        // sound pitch for car engine based on its current speed
        float speedPitch = (speed / maxSpeed) * (MAX_PITCH - IDLE_PITCH) + IDLE_PITCH;
        float currentPitch = carEngineComponent.currentPitch;

        // change pitch over time; take the world's time delta into account to make it independent of
        // the game's actual frame rate. World delta is in seconds.
        float pitchDelta = speedPitch * (getWorld().getDelta() / PITCH_FULL_RANGE_TIMESPAN);

        if (carEngineComponent.acceleratorPressed) {
            // If the accelerator is engaged, increase the sound pitch until it hits the pitch
            // associated with the current engine speed.
            if (currentPitch > speedPitch) {
                pitchDelta = -pitchDelta;
            }
            currentPitch = Math.min(speedPitch, currentPitch + pitchDelta);
        } else if (!carEngineComponent.acceleratorPressed && currentPitch > IDLE_PITCH) {
            // When the accelerator is disengaged, decrease the sound pitch to idle pitch.
            currentPitch = Math.max(IDLE_PITCH, currentPitch - pitchDelta);
        }

        carEngineComponent.currentPitch = currentPitch;
        ResourceManager.instance.carEngineSound.setPitch(carEngineComponent.carEngineSoundId,
                carEngineComponent.currentPitch);
    }
}
