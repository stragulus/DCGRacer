package org.avontuur.dcgracer.system;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import org.avontuur.dcgracer.utils.TrackingCamera;

/**
 * Display the game's HUD. Uses libgdx' scene2d.
 *
 * Created by Bram Avontuur on 2016-12-04.
 */

public class HUDDisplaySystem extends BaseSystem {
    private CameraUpdateSystem cameraUpdateSystem;

    private Stage stage;
    private Table table;
    private Skin skin;
    private Label offsetLabel;

    public HUDDisplaySystem() {
        // reference: https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L43
        // reference: https://github.com/libgdx/libgdx/wiki/Scene2d
        // reference: https://github.com/libgdx/libgdx/wiki/Scene2d.ui
        // reference: https://github.com/libgdx/libgdx/wiki/Table
        skin = new Skin();
        BitmapFont font = new BitmapFont();
        font.getData().setScale(4f);
        skin.add("default", font);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.GREEN;
        skin.add("default", labelStyle);

        stage = new Stage(new ExtendViewport(1920, 1080));
        table = new Table();
        table.setFillParent(true);
        table.left().top(); //align children from top-left corner
        table.setDebug(false);
        stage.addActor(table);

        offsetLabel = new Label("0.0", skin);
        offsetLabel.setColor(Color.GREEN);
        offsetLabel.setAlignment(Align.top | Align.left); // text alignment IN label
        table.add(offsetLabel).pad(10).align(Align.top | Align.left); // alignment of label in table
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    protected void processSystem() {
        updateOffset();
        stage.act(getWorld().getDelta());
        stage.draw();
    }

    private void updateOffset() {
        TrackingCamera cam = cameraUpdateSystem.getCamera(CameraEnum.STANDARD);
        float offset = cam.position.x - cam.viewportWidth / 2;
        offsetLabel.setText(String.format("%.2f", offset));
    }
}
