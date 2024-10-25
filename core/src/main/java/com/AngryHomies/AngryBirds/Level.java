package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;

public abstract class Level implements Screen {
    protected Main game;
    protected Stage stage;
    protected Skin skin;
    protected SpriteBatch batch;
    protected Texture pauseIconTexture;

    protected boolean isPaused;
    protected HashMap<String, Screen> levelScreens;
    protected HashMap<String, String> savedScreens;
    protected String currentLevel;


    protected TextButton winButton;
    protected TextButton loseButton;


    protected Slingshot slingshot;
    protected Texture groundTexture, catapultTexture;
    protected Bird[] birds;
    protected Pig[] pigs;
    protected Block[] blocks;

    public Level(Main game,HashMap<String, Screen> levelScreens,HashMap<String, String> savedScreens) {
        this.game = game;
        this.levelScreens = levelScreens != null ? levelScreens : new HashMap<>();
        this.savedScreens = savedScreens != null ? savedScreens : new HashMap<>();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin2/freezing-ui.json"));
        batch = new SpriteBatch();
        isPaused = false;


    pauseIconTexture = game.getAssetManager().get("images/pause_icon.png");

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (isPaused) {
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            stage.draw();
            handleKeyInput();
            return;
        }

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        winButton.setPosition(1650,1079-50);
        loseButton.setPosition(1600,1079-250);

        winButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("WinScreen")) {
                    levelScreens.put("WinScreen", new WinScreen(game, levelScreens));
                }
                game.setScreen(levelScreens.get("WinScreen"));
            }
        });


        loseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!levelScreens.containsKey("LoseScreen")) {
                    levelScreens.put("LoseScreen", new LoseScreen(game, levelScreens));
                }
                game.setScreen(levelScreens.get("LoseScreen"));
            }
        });
        batch.begin();
        renderGame(delta);
        batch.draw(pauseIconTexture, 20, Gdx.graphics.getHeight() - 150, 135, 135);
        batch.end();


        if (Gdx.input.isTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (x >= 20 && x <= 20 + 135 && y >= Gdx.graphics.getHeight() - 150 && y <= Gdx.graphics.getHeight() - 150 + 135) {
                showPauseMenu();
            }

        }
    }
    protected abstract void initializeLevel();
    protected abstract void renderGame(float delta);

    protected void showPauseMenu() {
        isPaused = true;
        stage.clear();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton restartButton = new TextButton("Restart", skin);
        TextButton saveButton = new TextButton("SaveGame", skin);
        TextButton exitButton = new TextButton("Exit to Home", skin);

        table.add(resumeButton).pad(10).row();
        table.add(restartButton).pad(10).row();
        table.add(saveButton).pad(10).row();
        table.add(exitButton).pad(10).row();

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hidePauseMenu();
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                restartLevel();
            }
        });

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveButton();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitToHomeScreen();
            }
        });
    }

    protected void saveButton() {

        transitionToSaveSuccessScreen();
    }

    private void transitionToSaveSuccessScreen() {

        Texture successTexture = game.getAssetManager().get("images/success_screen.png", Texture.class);


        SaveSuccessScreen saveSuccessScreen = new SaveSuccessScreen(game, levelScreens,successTexture, 600, 500,750,300);


        game.setScreen(saveSuccessScreen);
    }



    protected void hidePauseMenu() {
        isPaused = false;
        stage.clear();

    }

    protected void exitToHomeScreen() {
        Screen homeScreen = levelScreens.get("HomeScreen");
        if (homeScreen != null) {
            game.setScreen(homeScreen);
        } else {

            levelScreens.put("HomeScreen", new HomeScreen(game, levelScreens));
            game.setScreen(levelScreens.get("HomeScreen"));
        }
    }




    protected void handleKeyInput() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            hidePauseMenu();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            restartLevel();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exitToHomeScreen();
        }
    }

    private void restartLevel() {
        if (levelScreens != null) {
            if (levelScreens.containsKey("LevelOne")) {

                levelScreens.remove("LevelOne");


                levelScreens.put("LevelOne", new LevelOne(game, levelScreens, savedScreens));


                game.setScreen(levelScreens.get("LevelOne"));
            } else {

                levelScreens.put("LevelOne", new LevelOne(game, levelScreens, savedScreens));
                game.setScreen(levelScreens.get("LevelOne"));
            }
        } else {

            System.err.println("Game fault: levelScreens is null.");
        }
    }



    @Override
    public void resize(int width, int height) {
        if (stage!=null){
        stage.getViewport().update(width, height, true);
    }
    }

    @Override
    public void show() {
            Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
    @Override
    public void dispose() {

        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }

    }

}
