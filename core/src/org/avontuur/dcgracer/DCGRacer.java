package org.avontuur.dcgracer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.avontuur.dcgracer.manager.ScreenEnum;
import org.avontuur.dcgracer.manager.ScreenManager;

/**
 * Main class for the game
 *
 * Created by Bram Avontuur on 2016-02-28.
 */
public class DCGRacer extends Game {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		ScreenManager.getInstance().initialize(this);
		ScreenManager.getInstance().showScreen(ScreenEnum.GAME);
	}
}
