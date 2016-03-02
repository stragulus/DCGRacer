package org.avontuur.dcgracer.manager;

import com.badlogic.gdx.Screen;

import org.avontuur.dcgracer.screen.GameScreen;
import org.avontuur.dcgracer.screen.SplashScreen;

/**
 * This enum contains a reference to all the game screens. Add new screens (splash, main menu, etc)
 * here.
 *
 * Created by Bram Avontuur on 2016-02-28.
 */
public enum ScreenEnum {
    SPLASH {
        public Screen getScreen(Object... params) {
            return new SplashScreen();
        }
    },
    GAME {
        public Screen getScreen(Object... params) { return new GameScreen(); }
    };

    public abstract Screen getScreen(Object... params);
}
