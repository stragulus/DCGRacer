package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.DCGRacer;
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
        boolean died = false;
        float angleDegrees = body.getAngle() * MathUtils.radiansToDegrees % 360;

        if (body.getPosition().y < -10) {
            died = true;
        } else if (Math.abs(angleDegrees) > 150 && Math.abs(body.getLinearVelocity().x) < 0.001) {
            // At a large angle without speed. Could technically still recover (sliding down a hill), but
            // for now it's good. When turning this into an actual game, it runs out of fuel and only dies then.
            died = true;
            // TODO: giant Michael Bay-esque explosion!
        }

        if (died) {
            ScreenManager.getInstance().showScreen(ScreenEnum.GAMEOVER);
        }
    }
}
