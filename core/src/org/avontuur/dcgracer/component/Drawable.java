package org.avontuur.dcgracer.component;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Aspect of game objects that need to be drawn to the screen
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class Drawable extends Component {
    // A drawable has exactly one of these not set to null
    public PolygonRegion polygonRegion;
    public Sprite sprite;

    public Drawable() {}
}
