package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Draws entities with Sprite components to the screen.
 *
 * Created by Bram Avontuur on 2016-11-28.
 */

public class SpriteRenderingSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;
    private SpriteBatch batch;
    private CameraUpdateSystem cameraUpdateSystem;

    public SpriteRenderingSystem() {
        super(Aspect.all(org.avontuur.dcgracer.component.Sprite.class));
    }

    @Override
    protected void initialize() {
        batch = new SpriteBatch();
    }

    @Override
    protected void begin() {
        super.begin();
        // TODO: Move camera into this renderer? Or does that make it less generic..maybe have it have a
        //       setCamera()
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
    }

    @Override
    protected void process(int entityId) {
        Sprite sprite = mappers.spriteComponents.get(entityId).sprite;
        sprite.draw(batch);
    }

    @Override
    protected void end() {
        super.end();
        batch.end();
    }
}
