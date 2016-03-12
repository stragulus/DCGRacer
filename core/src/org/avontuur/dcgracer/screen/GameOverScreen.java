package org.avontuur.dcgracer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.avontuur.dcgracer.manager.ScreenEnum;
import org.avontuur.dcgracer.manager.ScreenManager;

/**
 * Game Over Screen
 *
 * Created by Bram Avontuur on 2016-03-12.
 */
public class GameOverScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private final long startTime;

    public GameOverScreen() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GlyphLayout glyphLayout = new GlyphLayout();
        String item = "HE DEAD";
        glyphLayout.setText(font, item);
        float positionX = Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2;
        float positionY = Gdx.graphics.getHeight() / 2 - glyphLayout.height / 2;

        batch.begin();
        font.draw(batch, item, positionX, positionY);
        batch.end();

        if (System.currentTimeMillis() - startTime > 3000) {
            ScreenManager.getInstance().showScreen(ScreenEnum.GAME);
        }
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
