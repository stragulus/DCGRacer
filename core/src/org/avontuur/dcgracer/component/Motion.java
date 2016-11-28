package org.avontuur.dcgracer.component;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bram Avontuur on 2016-11-26.
 *
 * Component containing data for objects that can be moved
 */

public class Motion extends Component {
    public Vector2 force;

    public Motion() {
        force = new Vector2(0, 0);
    }
}
