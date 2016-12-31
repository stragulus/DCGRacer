package org.avontuur.dcgracer.component;

import com.artemis.Component;

/**
 * Data associated with a car's engine.
 * Created by Bram Avontuur on 2016-12-30.
 */

public class CarEngine extends Component {
    public CarEngine() {
    }

    // ID to Sound instance playing car engine sound
    public long carEngineSoundId = -1;
    // current pitch of car engine sound
    public float currentPitch = 1;
    // whether the car's accelerator is pressed
    public boolean acceleratorPressed = false;
}
