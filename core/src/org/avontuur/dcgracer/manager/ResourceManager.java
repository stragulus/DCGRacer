package org.avontuur.dcgracer.manager;

/**
 * Created by Bram Avontuur on 2016-11-26.
 *
 * This class manages all static assets.
 */

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import org.avontuur.dcgracer.DCGRacer;

public class ResourceManager {
    public static final ResourceManager instance = new ResourceManager();

    // textures
    public Texture textureTerrainMud;
    public Texture gameLogo;
    public Texture carBody;
    public Texture jerryCan;
    public Texture wheel;
    public Texture coin;

    // audio samples
    public Sound carEngineSound;

    // singleton: prevent instantiation from other classes
    private ResourceManager() {
    }

    private void loadTextures() {
        textureTerrainMud = new Texture(Gdx.files.internal("background_mud.png"));
        gameLogo = new Texture(Gdx.files.internal("ball.png"));
        carBody = new Texture(Gdx.files.internal("car body.png"));
        jerryCan = new Texture(Gdx.files.internal("jerrycan.png"));
        wheel = new Texture(Gdx.files.internal("wheel.png"));
        coin = new Texture(Gdx.files.internal("coin.png"));
    }

    private void loadSoundEffects() {

        carEngineSound = Gdx.audio.newSound(Gdx.files.internal("car engine.wav"));
    }

    private void loadMusic() {
    }

    public void loadAll() {
        DCGRacer.log.info("Loading resources");
        loadMusic();
        loadSoundEffects();
        loadTextures();
        DCGRacer.log.info("Loaded all resources");
    }

    public void stopSounds() {
        carEngineSound.stop();
    }
}