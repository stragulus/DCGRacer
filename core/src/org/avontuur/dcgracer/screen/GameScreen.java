package org.avontuur.dcgracer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.avontuur.dcgracer.base.BaseScreen;

/**
 * This class represents the actual game play.
 *
 * Created by Bram Avontuur on 2016-02-28.
 */
public class GameScreen extends BaseScreen {

    private Texture txtrGameLogo;

    public GameScreen() {
        super();
        txtrGameLogo = new Texture(Gdx.files.internal("badlogic.jpg"));
    }

    @Override
    public void buildStage() {
        Image logo = new Image(txtrGameLogo);
        addActor(logo);
    }

    @Override
    public void dispose() {
        super.dispose();
        txtrGameLogo.dispose();
    }
}
