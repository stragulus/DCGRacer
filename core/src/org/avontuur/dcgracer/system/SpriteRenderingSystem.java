package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import org.avontuur.dcgracer.DCGRacer;
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
    private Box2dWorldSystem box2dWorldSystem;
    private Box2DDebugRenderer debugRenderer;
    public SpriteRenderingSystem() {
        super(Aspect.all(org.avontuur.dcgracer.component.Sprite.class));
        debugRenderer = new Box2DDebugRenderer();
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
        //DCGRacer.log.debug("Sprite " + sprite + " position = " + sprite.getX() + ", " + sprite.getY());
    }

    @Override
    protected void end() {
        super.end();
        batch.end();
        debugRenderer.render(box2dWorldSystem.getBox2DWorld(), cameraUpdateSystem.getCamera(CameraEnum.STANDARD).combined);
    }
}
