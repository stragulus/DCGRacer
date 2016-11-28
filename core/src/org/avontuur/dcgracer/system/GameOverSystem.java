package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.component.MainPlayer;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ScreenEnum;
import org.avontuur.dcgracer.manager.ScreenManager;

/**
 * Checks whether the game is over.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class GameOverSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public GameOverSystem() {
        super(Aspect.all(MainPlayer.class, Physics.class));
    }

    @Override
    protected void process(int entityId) {
        Body body = mappers.physicsComponents.get(entityId).body;

        if (body.getPosition().y < 0) {
            ScreenManager.getInstance().showScreen(ScreenEnum.GAMEOVER);
        }

    }
}
