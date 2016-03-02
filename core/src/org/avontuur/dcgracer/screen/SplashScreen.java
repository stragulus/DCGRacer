package org.avontuur.dcgracer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.avontuur.dcgracer.base.BaseScene2DScreen;

/**
 * This class represents the actual game play.
 *
 * Created by Bram Avontuur on 2016-02-28.
 */
public class SplashScreen implements Screen {
    private SpriteBatch batch;
    private Texture txtrGameLogo;

    public SplashScreen() {
        batch = new SpriteBatch();
        txtrGameLogo = new Texture(Gdx.files.internal("badlogic.jpg"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(txtrGameLogo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
    }

    @Override
    public void dispose() {
        txtrGameLogo.dispose();
        batch.dispose();
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
}
