package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;

import org.avontuur.dcgracer.component.CameraID;
import org.avontuur.dcgracer.component.Drawable;
import org.avontuur.dcgracer.component.MainPlayer;
import org.avontuur.dcgracer.component.Motion;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.component.PlayerInput;

/**
 * Placeholder system for all the various component mappers used throughout the game.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class ComponentMapperSystem extends BaseSystem {
    public ComponentMapper<Physics> physicsComponents;
    public ComponentMapper<MainPlayer> mainPlayerComponents;
    public ComponentMapper<Drawable> drawableComponents;
    public ComponentMapper<CameraID> cameraIDComponents;
    public ComponentMapper<Motion> motionComponents;
    public ComponentMapper<PlayerInput> playerInputComponents;


    public ComponentMapperSystem() {
    }

    @Override
    protected boolean checkProcessing() {
        return false;
    }

    @Override
    protected void processSystem() {
        // Nothing to do here
    }
}
