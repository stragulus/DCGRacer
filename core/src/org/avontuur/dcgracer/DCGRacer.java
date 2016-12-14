package org.avontuur.dcgracer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Timer;

import org.avontuur.dcgracer.manager.ResourceManager;
import org.avontuur.dcgracer.manager.ScreenEnum;
import org.avontuur.dcgracer.manager.ScreenManager;


/**
 * Main class for the game
 * <p/>
 * Created by Bram Avontuur on 2016-02-28.
 */
public class DCGRacer extends Game {

    private static long SPLASH_MINIMUM_MILLIS = 2000L;

    public static final Logger log = new Logger("Game", Logger.DEBUG);

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        ScreenManager.getInstance().initialize(this);
        ScreenManager.getInstance().showScreen(ScreenEnum.SPLASH);

        final long splash_start_time = System.currentTimeMillis();

        // load resources here, while the splash screen is being displayed.
        ResourceManager.instance.loadAll();

        // Show the splash screen for at least a few seconds so users will see it.
        // The splash screen does not depend on resource loading (e.g. ResourceManager)
        // so it will load fast. Any resources being loaded can now load while the splash
        // screen is already being displayed.
        new Thread(new Runnable() {
            @Override
            public void run() {

                // postRunnable: this will make sure the code will run in the render thread. This is
                // necessary because Game.setScreen() needs to run in the render thread (called
                // by ScreenManager.showScreen)
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        DCGRacer.log.debug("Post-resource load splash-screen");
                        long splash_elapsed_time = System.currentTimeMillis() - splash_start_time;
                        if (splash_elapsed_time < DCGRacer.SPLASH_MINIMUM_MILLIS) {
                            float sleepTime = (DCGRacer.SPLASH_MINIMUM_MILLIS - splash_elapsed_time) / 1000f;
                            DCGRacer.log.debug("Sleep time left: " + sleepTime);
                            Timer.schedule(
                                    new Timer.Task() {
                                        @Override
                                        public void run() {
                                            DCGRacer.log.debug("Done sleeping; it's game time!");
                                            ScreenManager.getInstance().showScreen(ScreenEnum.GAME);
                                        }
                                    }, sleepTime);
                        } else {
                            DCGRacer.log.debug("No time to sleep, let's play!");
                            ScreenManager.getInstance().showScreen(ScreenEnum.GAME);
                        }
                    }
                });
            }
        }).start();
    }
}
