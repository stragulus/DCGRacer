package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

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

        if (sprite == null) {
            return;
        }

        Shape.Type shapeType = body.getFixtureList().get(0).getShape().getType();
        if (shapeType == Shape.Type.Polygon) {
            sprite.setPosition(body.getPosition().x, body.getPosition().y);
        } else {
            // circle and box shapes have the body origin in the center; other shapes have the origin in the bottom
            // left. Not sure if boxes are actually not also of Polygon type, in which case I have to do this
            // differently.
            sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2,
                    body.getPosition().y - sprite.getHeight() / 2);
        }
        // TODO: Should probably use a unique component for rotatables.
        sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);

    }
}
