package org.avontuur.dcgracer.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Body;

import org.avontuur.dcgracer.DCGRacer;
import org.avontuur.dcgracer.component.MainPlayer;
import org.avontuur.dcgracer.component.Physics;
import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Contains all the in-game cameras, and has them track the main player.
 *
 * Created by Bram Avontuur on 2016-11-27.
 */

public class CameraUpdateSystem extends IteratingSystem {
    private ComponentMapperSystem mappers;

    public TrackingCamera cam;
    public TrackingCamera terrainCam;

    // width of viewport on the world, in world units
    private float viewportWidth;

    public CameraUpdateSystem(float viewportWidth) {
        super(Aspect.all(Physics.class, MainPlayer.class));

        this.viewportWidth = viewportWidth;
    }

    @Override
    protected void initialize() {
        super.initialize();

        cam = setupCamera(1);

        // Drawing the terrain appears to need a different pixels-to-meters ratio. With repeating textures, it will
        // use the texture pixels as unit. So we need to convert those to meters. I want the ground texture to be
        // about 1m wide, so ratio is 1m = <texture width> / 2
        float terrainPPM = ResourceManager.instance.textureTerrainMud.getWidth() / 2;
        terrainCam = setupCamera(terrainPPM);
    }

    @Override
    protected void process(int entityId) {
        Body playerBody = mappers.physicsComponents.get(entityId).body;
        cam.center(playerBody, world.getDelta());
        cam.update();
        terrainCam.center(playerBody, world.getDelta());
        terrainCam.update();
    }

    /**
     * Get camera instance associated with a camera ID
     *
     * @param cameraID ID of camera
     * @return {@link TrackingCamera} instance
     */
    public TrackingCamera getCamera(CameraEnum cameraID) {
        if (cameraID == CameraEnum.STANDARD) {
            return cam;
        }
        return terrainCam;
    }

    public void resize(int width, int height) {
        cam.resize(viewportWidth, viewportWidth * height / width);
        cam.update();
        terrainCam.resize(viewportWidth, viewportWidth * height / width);
        terrainCam.update();
    }

    private TrackingCamera setupCamera(final float unitsPerMeter) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Constructs a new TrackingCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio. The Camera's units match the Physics' world
        // units. This requires that all sprites have their world size set explicitly, so
        // that we never have to worry about pixels anymore.

        // lerp: Adds a delay to smooth the camera movements. Bigger value implies faster movements.
        float lerp = 2f;
        TrackingCamera cam = new TrackingCamera(this.viewportWidth, this.viewportWidth * (h / w),
                unitsPerMeter, lerp, true, false);
        cam.setBoundaryLeft(0); //don't pan past the left side of the world

        // set camera in bottom-left corner. Position is in the center of the viewport, so adjust
        // accordingly.
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        return cam;
    }


}
