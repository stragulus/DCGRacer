package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.Physics;

/**
 * Updates sprite position and rotation for entities that have a presence in the physics world
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class SpritePositionSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public SpritePositionSystem() {
        super(Aspect.all(org.avontuur.dcgracer.component.Sprite.class, Physics.class));
    }

    @Override
    protected void process(int entityId) {
        Sprite sprite = mappers.spriteComponents.get(entityId).sprite;
        Body body = mappers.physicsComponents.get(entityId).body;

        /**
         * TODO: another reason to split up drawable into Sprite & Polygon (see {@link CameraUpdateSystem})
         */
        if (sprite == null) return;

        //DCGRacer.log.debug("Setting sprite " + sprite + " position to body " + body + " position " + body.getPosition());

        // TODO: The ball body/sprite position are offset, but the car body/sprite are not. Fix this!
        // Now update the sprite position accordingly to its now updated Physics playerBody
        // THIS works for the ball, not the car body
        //sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
        // TODO: Should probably use a unique component for rotatables.
        sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);

    }
}
