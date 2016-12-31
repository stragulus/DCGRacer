package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;

import org.avontuur.dcgracer.component.Motion;
import org.avontuur.dcgracer.component.PlayerInput;

/**
 * Scans the player input state and applies motion to the player entity based on state.
 *
 * Created by Bram Avontuur on 2016-12-31.
 */

public class PlayerInputMotionSystem extends IteratingSystem {
    // Amount of horizontal speed applied to wheel of player's car
    public static final float WHEEL_SPEED = 10f;

    private ComponentMapperSystem mappers;

    public PlayerInputMotionSystem() {
        super(Aspect.all(PlayerInput.class, Motion.class));
    }

    @Override
    protected void process(int entityId) {
        PlayerInput playerInputComponent = mappers.playerInputComponents.get(entityId);
        Motion motion = mappers.motionComponents.get(entityId);
        float accelerate = playerInputComponent.accelerate;

        if (accelerate > 0) {
            motion.force.x = WHEEL_SPEED;
            motion.force.y = 0;
        } else if (accelerate < 0) {
            motion.force.x = -WHEEL_SPEED;
            motion.force.y = 0;
        } else {
            motion.force.x = 0;
            motion.force.y = 0;
        }

    }
}
