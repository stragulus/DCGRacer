package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Draws entities with Sprite components to the screen.
 * 
 * Created by Bram Avontuur on 2016-11-28.
 */

public class PolygonRegionRenderingSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;
    private PolygonSpriteBatch batch;
    private CameraUpdateSystem cameraUpdateSystem;

    public PolygonRegionRenderingSystem() {
        super(Aspect.all(org.avontuur.dcgracer.component.PolygonRegion.class));
    }

    @Override
    protected void initialize() {
        batch = new PolygonSpriteBatch();
    }

    @Override
    protected void begin() {
        super.begin();
        // TODO: Move camera into this renderer? Or does that make it less generic..maybe have it have a
        //       setCamera()
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.TERRAIN);
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
    }

    @Override
    protected void process(int entityId) {
        PolygonRegion polygonRegion = mappers.polygonRegionComponents.get(entityId).polygonRegion;
        batch.draw(polygonRegion, 0, 0);
    }

    @Override
    protected void end() {
        super.end();
        batch.end();
    }
}
