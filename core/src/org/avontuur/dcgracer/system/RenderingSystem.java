package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.avontuur.dcgracer.component.CameraID;
import org.avontuur.dcgracer.component.Drawable;
import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Renders drawable game entities to the screen.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class RenderingSystem extends BaseEntitySystem {
    private ComponentMapperSystem mappers;
    private CameraUpdateSystem cameraUpdateSystem;


    // sprite batch used to draw non-terrain objects
    private SpriteBatch batch;
    // sprite batch used to draw terrain objects
    // TODO: Should I not just have a PolygenRenderSystem and a SpriteRenderSystem? Isn't that the only
    //       distinction? Then all this would be way neater. They'd all each have their own camera. And you'd
    //       have Polygon & Sprite components.
    private PolygonSpriteBatch terrainBatch;

    public RenderingSystem() {
        super(Aspect.all(Drawable.class, CameraID.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        terrainBatch = new PolygonSpriteBatch();
        batch = new SpriteBatch();
    }

    @Override
    protected final void processSystem() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        IntBag drawables = subscription.getEntities();
        int[] ids = drawables.getData();

        // Entities are drawn using different cameras. Group the entities by cameraID first, then
        // for each camera create a spriteBatch and draw them on the screen

        // It's probably faster to iterate over all the entities than to construct a hashmap here..
        for (CameraEnum cameraID: CameraEnum.values()) {
            drawToCamera(ids, cameraID);
        }
    }

    private void drawToCamera(int[] ids, CameraEnum cameraID) {
        TrackingCamera cam = cameraUpdateSystem.getCamera(cameraID);
        Batch renderBatch = batch;

        if (cameraID == CameraEnum.TERRAIN) {
            renderBatch = terrainBatch;
        }

        renderBatch.setProjectionMatrix(cam.combined);
        renderBatch.begin();

        for (int entityID: ids) {
            if (mappers.cameraIDComponents.get(entityID).cameraID ==cameraID) {
                Drawable drawable = mappers.drawableComponents.get(entityID);
                if (drawable.sprite != null) {
                    drawable.sprite.draw(renderBatch);
                } else {
                    // FIXME I am assuming here that drawables with a polygon region
                    //       are always terrain, regardless of cameraID. If that is true,
                    //       then I need to split this code into 2 different renderers like in the
                    //       TO-DO note above.
                    terrainBatch.draw(drawable.polygonRegion, 0, 0);
                }
            }
        }

        renderBatch.end();
    }
}
