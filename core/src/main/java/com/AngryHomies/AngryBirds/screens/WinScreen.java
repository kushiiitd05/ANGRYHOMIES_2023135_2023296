package com.AngryHomies.AngryBirds.screens;

import com.AngryHomies.AngryBirds.assetManager.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class WinScreen implements Screen {
    private Main game;
    private Stage stage;
    private Texture winTexture;
    private ImageButton backButton;
    private HashMap<String, Screen> levelScreens;

    public WinScreen(Main game, HashMap<String, Screen> levelScreens) {
        this.game = game;
        this.levelScreens = levelScreens;
        stage = new Stage(new ScreenViewport());

        winTexture = game.getAssetManager().get("images/win_screen.png");

        Texture backButtonTexture = game.getAssetManager().get("images/back_button.png");
        backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.setPosition(30, 25);
        backButton.setSize(150, 150);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("LevelSelectScreen")) {
                    levelScreens.put("LevelSelectScreen", new LevelSelectScreen(game, levelScreens, null));
                    game.setScreen(levelScreens.get("LevelSelectScreen"));
                }
                game.setScreen(levelScreens.get("LevelSelectScreen"));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(winTexture, 0, 0, 650, 530);
        game.batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (winTexture != null) {
            winTexture.dispose();
            winTexture = null;
        }
    }

    @Override
    public void show() {
        stage.clear();
        Gdx.input.setInputProcessor(stage);

        if (backButton != null) {
            stage.addActor(backButton);
        }
    }

    @Override
    public void hide() {
        stage.clear();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}

