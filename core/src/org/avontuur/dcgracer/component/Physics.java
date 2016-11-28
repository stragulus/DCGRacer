package org.avontuur.dcgracer.component;

import com.artemis.Component;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by Bram Avontuur on 2016-04-04.
 *
 * Components that holds a box2d Body object.
 */
public class Physics extends Component {
    public Body body;

    public Physics() {}
}
