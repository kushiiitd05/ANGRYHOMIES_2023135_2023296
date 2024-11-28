package com.AngryHomies.AngryBirds.screens;

import com.AngryHomies.AngryBirds.assetManager.Main;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelOne;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelThree;
import com.AngryHomies.AngryBirds.logic.level.levels.LevelTwo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class LevelSelectScreen implements Screen {

    private Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private int selectedButtonIndex = 0;

    private Texture Selectlevel_logo;
    private Texture backButtonTexture;
    private ImageButton backButton;
    private ImageButton level1Button;
    private ImageButton level2Button;
    private ImageButton level3Button;

    public LevelSelectScreen(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savescreen) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
//        Gdx.input.setInputProcessor(stage);


        backgroundTexture = game.getAssetManager().get("images/level_bg.png");
        backButtonTexture = game.getAssetManager().get("images/back_button.png");
        Selectlevel_logo = game.getAssetManager().get("images/lvl_select_logo.png");




        skin = new Skin(Gdx.files.internal("uiskin/uiskin1/uiskin.json"));


        backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.setPosition(30, 25);
        backButton.setSize(150, 150);
        stage.addActor(backButton);


        Texture level1Texture = game.getAssetManager().get("images/lvl1.png");
        Texture level2Texture  =game.getAssetManager().get("images/lvl2.png");
        Texture level3Texture = game.getAssetManager().get("images/lvl3.png");


        this.level1Button = new ImageButton(new TextureRegionDrawable(level1Texture));
        this.level2Button = new ImageButton(new TextureRegionDrawable(level2Texture));
        this.level3Button = new ImageButton(new TextureRegionDrawable(level3Texture));

        level1Button.getImageCell().width(150).height(150);
        level2Button.getImageCell().width(150).height(150);
        level3Button.getImageCell().width(150).height(150);

        level1Button.setPosition(300, 500);
        level2Button.setPosition(1079 - 218, 500);
        level3Button.setPosition(1400, 500);




        stage.addActor(level1Button);
        stage.addActor(level2Button);
        stage.addActor(level3Button);


        addClickListeners(levelScreens);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        handleKeyboardInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0,650, 530);
        game.batch.draw(Selectlevel_logo, 205, 285,250,95);
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    private void handleKeyboardInput() {

    }

    private void addClickListeners(HashMap<String, Screen> levelScreens) {
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("HomeScreen")) {
                    levelScreens.put("HomeScreen", new LevelOne(game, levelScreens));
                }
                game.setScreen(levelScreens.get("HomeScreen"));
            }
        });

        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Screen levelOne = new LevelOne(game, levelScreens);
                levelScreens.put("LevelOne", levelOne);
                game.setScreen(levelOne);
            }
        });


        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Screen levelTwo = new LevelTwo(game, levelScreens);
                levelScreens.put("LevelTwo", levelTwo);
                game.setScreen(levelTwo);
            }
        });

        level3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Screen levelThree = new LevelThree(game, levelScreens);
                levelScreens.put("LevelThree", levelThree);
                game.setScreen(levelThree);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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

    @Override
    public void dispose() {

    }
}

