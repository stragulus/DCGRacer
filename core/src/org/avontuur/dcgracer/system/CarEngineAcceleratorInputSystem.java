package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;

import org.avontuur.dcgracer.component.CarEngine;
import org.avontuur.dcgracer.component.PlayerInput;

/**
 * Based on UI input, signal whether a car engine's accelerator is engaged.
 *
 * Created by Bram Avontuur on 2016-12-31.
 */

public class CarEngineAcceleratorInputSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public CarEngineAcceleratorInputSystem() {
        super(Aspect.all(PlayerInput.class, CarEngine.class));
    }

    @Override
    protected void process(int entityId) {
        float accelerate = mappers.playerInputComponents.get(entityId).accelerate;
        CarEngine carEngineComponent = mappers.carEngineComponents.get(entityId);

        // TODO: reversing not taken into acccount; ditch reversing or also use it here.
        carEngineComponent.acceleratorPressed = accelerate > 0;
    }
}
