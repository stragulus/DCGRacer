package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;

import org.avontuur.dcgracer.component.Motion;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.component.WheelJoint;

/**
 * Sets wheel motor speed based on a motion vector.
 * Created by Bram Avontuur on 2016-12-28.
 */

public class WheelSpeedSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public WheelSpeedSystem() {
        super(Aspect.all(WheelJoint.class, Motion.class));
    }

    @Override
    protected void process(int entityId) {
        Motion motionComponent = mappers.motionComponents.get(entityId);
        WheelJoint wheelJointComponent = mappers.wheelJointComponents.get(entityId);
        wheelJointComponent.wheelJoint.enableMotor(true);
        wheelJointComponent.wheelJoint.setMotorSpeed(motionComponent.force.x);
    }
}
