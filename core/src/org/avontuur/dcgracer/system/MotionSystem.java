package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Motion;
import org.avontuur.dcgracer.component.Physics;

/**
 * Created by Bram Avontuur on 2016-11-26.
 *
 * Apply force to the physics component of entities that have been instructed to move by force.
 */

public class MotionSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public MotionSystem() {
        super(Aspect.all(Physics.class, Motion.class));

    }
    @Override
    protected void process(int entityId) {
        Motion motionComponent = mappers.motionComponents.get(entityId);

        if (motionComponent.force.x != 0 || motionComponent.force.y != 0) {
            Physics physicsComponent = mappers.physicsComponents.get(entityId);
            physicsComponent.body.applyForceToCenter(motionComponent.force, true);
        }

    }
}
