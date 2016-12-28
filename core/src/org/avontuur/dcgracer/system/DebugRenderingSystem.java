package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

/**
 * Created by Bram Avontuur on 2016-12-27.
 */

public class DebugRenderingSystem extends BaseSystem {
    private Box2dWorldSystem box2dWorldSystem;
    private CameraUpdateSystem cameraUpdateSystem;

    private Box2DDebugRenderer debugRenderer;

    public DebugRenderingSystem() {
        this.debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    protected void processSystem() {
        debugRenderer.render(box2dWorldSystem.getBox2DWorld(), cameraUpdateSystem.getCamera(CameraEnum.STANDARD).combined);

    }
}
