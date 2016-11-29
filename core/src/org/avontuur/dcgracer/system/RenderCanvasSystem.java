package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Draws a clean new canvas for other renderers to paint on top of.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class RenderCanvasSystem extends BaseSystem {
    public RenderCanvasSystem() {
    }

    @Override
    protected final void processSystem() {
        // clear the screen
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
