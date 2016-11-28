package org.avontuur.dcgracer.manager;

/**
 * Created by Bram Avontuur on 2016-11-26.
 *
 * This class manages all static assets.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class ResourceManager {
    public static final ResourceManager instance = new ResourceManager();

    // textures
    public Texture textureTerrainMud;
    public Texture gameLogo;

    // singleton: prevent instantiation from other classes
    private ResourceManager() {
    }

    private void loadTextures() {
        textureTerrainMud = new Texture(Gdx.files.internal("background_mud.png"));
        gameLogo = new Texture(Gdx.files.internal("ball.png"));
    }

    private void loadSoundEffects() {
    }

    private void loadMusic() {
    }

    public void loadAll() {
        loadMusic();
        loadSoundEffects();
        loadTextures();
    }
}