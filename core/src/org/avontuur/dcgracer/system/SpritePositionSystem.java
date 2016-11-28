package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.component.Drawable;
import org.avontuur.dcgracer.component.Physics;

/**
 * Updates sprite position and rotation for entities that have a presence in the physics world
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class SpritePositionSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public SpritePositionSystem() {
        super(Aspect.all(Drawable.class, Physics.class));
    }

    @Override
    protected void process(int entityId) {
        Sprite sprite = mappers.drawableComponents.get(entityId).sprite;
        Body body = mappers.physicsComponents.get(entityId).body;

        /**
         * TODO: another reason to split up drawable into Sprite & Polygon (see {@link CameraUpdateSystem})
         */
        if (sprite == null) return;

        // Now update the sprite position accordingly to its now updated Physics playerBody
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        // TODO: Should probably use a unique component for rotatables.
        sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);

    }
}
