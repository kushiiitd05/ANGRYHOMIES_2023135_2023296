package com.AngryHomies.AngryBirds.screens;

import com.AngryHomies.AngryBirds.Serializables.LevelState;
import com.AngryHomies.AngryBirds.assetManager.Main;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelOne;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelThree;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelTwo;
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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import static com.AngryHomies.AngryBirds.logic.level.Level.DEFAULT_SAVE_PATH;

public class LoadScreen implements Screen {

    private Main game;
    private Stage stage;
    private Texture backgroundTexture;
    private HashMap<String, Screen> levelScreens;
    private HashMap<String, String> savedScreens;

    private Texture loadScreenLogo;
    private Texture backButtonTexture;
    private ImageButton backButton;
    private ImageButton saveGame1Button;
    private ImageButton saveGame2Button;
    private ImageButton saveGame3Button;

    public LoadScreen(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savedScreens) {
        this.game = game;
        this.levelScreens = levelScreens;
        this.savedScreens = savedScreens != null ? savedScreens : new HashMap<>();

        stage = new Stage(new ScreenViewport());

        backgroundTexture = game.getAssetManager().get("images/load_bg.png", Texture.class);
        backButtonTexture = game.getAssetManager().get("images/back_button.png", Texture.class);
        loadScreenLogo = game.getAssetManager().get("images/lvl_select_logo.png", Texture.class);

        backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.setPosition(30, 25);
        backButton.setSize(150, 150);
        stage.addActor(backButton);

        Texture saveGame1Texture = game.getAssetManager().get("images/lvl1.png", Texture.class);
        Texture saveGame2Texture = game.getAssetManager().get("images/lvl2.png", Texture.class);
        Texture saveGame3Texture = game.getAssetManager().get("images/lvl3.png", Texture.class);

        saveGame1Button = new ImageButton(new TextureRegionDrawable(saveGame1Texture));
        saveGame2Button = new ImageButton(new TextureRegionDrawable(saveGame2Texture));
        saveGame3Button = new ImageButton(new TextureRegionDrawable(saveGame3Texture));

        saveGame1Button.getImageCell().width(150).height(150);
        saveGame2Button.getImageCell().width(150).height(150);
        saveGame3Button.getImageCell().width(150).height(150);

        saveGame1Button.setPosition(300, 700);
        saveGame2Button.setPosition(861, 700);
        saveGame3Button.setPosition(1400, 700);

        stage.addActor(saveGame1Button);
        stage.addActor(saveGame2Button);
        stage.addActor(saveGame3Button);

        addClickListeners();
    }

    private void addClickListeners() {
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(levelScreens.get("HomeScreen"));
            }
        });

        saveGame1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadLevel("LevelOne", levelScreens);
            }
        });

        saveGame2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadLevel("LevelTwo", levelScreens);
            }
        });

        saveGame3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadLevel("LevelThree", levelScreens);
            }
        });
    }

    private void loadLevel(String level, HashMap<String, Screen> levelScreens) {
        try {
            FileInputStream fileIn = new FileInputStream(DEFAULT_SAVE_PATH + "_" + level + ".sav");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            LevelState levelState = (LevelState) in.readObject();
            in.close();
            fileIn.close();

            Screen screenLevel = null;
            if (level.equals("LevelOne")) {
                screenLevel = new LevelOne(game, levelState, levelScreens);
            } else if (level.equals("LevelTwo")) {
                screenLevel = new LevelTwo(game, levelState, levelScreens);
            } else if (level.equals("LevelThree")) {
                screenLevel = new LevelThree(game, levelState, levelScreens);
            } else {
                return;
            }

            this.levelScreens.put(level, screenLevel);
            game.setScreen(screenLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, 650, 500);
        game.batch.draw(loadScreenLogo, 861, 600, 400, 150);
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {}
}

