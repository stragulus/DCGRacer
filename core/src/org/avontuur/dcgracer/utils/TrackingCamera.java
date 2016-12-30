package org.avontuur.dcgracer.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * An Orthogonal camera that tracks an object in the world.
 *
 * Created by Bram Avontuur on 2016-03-20.
 */
public class TrackingCamera extends OrthographicCamera {
    private float unitsPerMeter;
    private float lerp;
    private boolean centerX;
    private boolean centerY;
    private Float boundaryLeft = null;
    private Float boundaryRight = null;
    private Float boundaryTop = null;
    private Float boundaryBottom = null;

    /**
     * Instantiates a new tracking camera.
     *
     * @param worldWidth Width of the viewport, in world units
     * @param worldHeight Height of the viewport, in world units
     * @param unitsPerMeter Conversion factor to transform wold units to this camera's units
     * @param lerp Lerp factor, determines whether camera will have a delay in tracking an object. 0 <= lerp <= 1;
     *             the closer it is to 1, the less it will delay
     * @param centerX True if camera should try to keep object being tracked horizontally centered
     * @param centerY True if camera should try to keep object being tracked vertically centered
     */
    public TrackingCamera(float worldWidth, float worldHeight, float unitsPerMeter, float lerp,
                          boolean centerX, boolean centerY) {
        super(worldWidth * unitsPerMeter, worldHeight * unitsPerMeter);

        this.unitsPerMeter = unitsPerMeter;
        this.lerp = lerp;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Clamp camera movement to the left
     * @param boundaryLeft
     *        X coordinate, in world space, of left boundary.
     */
    public void setBoundaryLeft(float boundaryLeft) {
        this.boundaryLeft = boundaryLeft * unitsPerMeter;
    }

    /**
     * Clamp camera movement to the right
     * @param boundaryRight
     *        X coordinate, in world space, of left boundary.
     */
    public void setBoundaryRight(float boundaryRight) {
        this.boundaryRight = boundaryRight * unitsPerMeter;
    }

    /**
     * Clamp camera movement to the top
     * @param boundaryTop
     *        X coordinate, in world space, of left boundary.
     */
    public void setBoundaryTop(float boundaryTop) {
        this.boundaryTop = boundaryTop * unitsPerMeter;
    }

    /**
     * Clamp camera movement to the bottom
     * @param boundaryBottom
     *        X coordinate, in world space, of left boundary.
     */
    public void setBoundaryBottom(float boundaryBottom) {
        this.boundaryBottom = boundaryBottom * unitsPerMeter;
    }

    /**
     * Resize the camera viewport using world units
     * @param worldWidth New viewport width, in world units
     * @param worldHeight New viewport height, in world units
     */
    public void resize(float worldWidth, float worldHeight) {
        viewportWidth = worldWidth * unitsPerMeter;
        viewportHeight = worldHeight * unitsPerMeter;
    }

    /**
     * Center the camera on a box2d body
     * @param body Box2d body to center on; position must be in world units, not camera units
     * @param deltaTime Time elapsed since last call, used for lerping
     */
    public void center(Body body, float deltaTime) {
        translate(
                centerXorY(body.getWorldCenter().x, position.x, viewportWidth, boundaryLeft, boundaryRight,
                        deltaTime, centerX),
                centerXorY(body.getWorldCenter().y, position.y, viewportHeight, boundaryTop, boundaryBottom,
                        deltaTime, centerY)
        );
    }

    /*
     * Returns the delta by which the provided camPosition needs to be altered
     */
    private float centerXorY(float bodyPositionWorld, float camPosition, float viewport, Float minBoundary,
                             Float maxBoundary, float deltaTime, boolean doCenter) {
        if (!doCenter) {
            return 0f;
        }

        float bodyPosition = bodyPositionWorld * unitsPerMeter;
        //float delta = (bodyPosition - camPosition) * lerp * deltaTime;
        float delta = (bodyPosition - camPosition);
        // TODO: Disabled lerp for now; it will allow the player to move out of the viewport, which I don't want.
        //float lerpFactor = MathUtils.clamp(this.lerp * deltaTime, 0f, 1f);
        float newPosition = camPosition + delta;

        // never move the camera viewport beyond the (horizontal) edges of the world.
        if (minBoundary != null && newPosition < minBoundary + (viewport / 2f)) {
            delta = 0;
        } else if (maxBoundary != null && newPosition > maxBoundary - (viewport / 2f)) {
            delta = 0;
        }

        return delta;
    }

}
