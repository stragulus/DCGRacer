package org.avontuur.dcgracer.component;

import com.artemis.Component;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;

/**
 * Component that signals which entity will be controlled by the user.
 * Created by Bram Avontuur on 2016-11-27.
 */

public class PlayerInput extends Component {
    // Accelerator/decelerator action. <0: Decelerator, >0: accelerator
    public int accelerate;

    public PlayerInput() {
    }

}
