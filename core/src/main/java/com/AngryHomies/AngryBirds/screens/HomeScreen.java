package com.AngryHomies.AngryBirds.screens;

import com.AngryHomies.AngryBirds.assetManager.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.HashMap;

public class HomeScreen implements Screen {

    private Main game;
    private Stage stage;
    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture logoTexture;
    private TextButton newGameButton, loadGameButton, quitGameButton;
    private Skin skin;
    private Music music;
    private HashMap<String, Screen> levelScreens;
    private HashMap<String, String> savescreen;
    private int selectedButtonIndex = 0;
    private TextButton[] buttons;

    public HomeScreen(Main game, Music loadingMusic, HashMap<String, Screen> levelScreens, HashMap<String, String> savescreen) {
        this.game = game;
        this.music = loadingMusic;
        this.levelScreens = levelScreens;
        this.savescreen = savescreen;
    }

    public HomeScreen(Main game, HashMap<String, Screen> levelScreens) {
        this.game = game;
        this.levelScreens = levelScreens;
    }

    public HomeScreen(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savescreens) {
        this(game, levelScreens);
        this.savescreen = savescreens;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = game.getAssetManager().get("images/angry_bird_image.png", Texture.class);
        logoTexture = game.getAssetManager().get("images/logo.png", Texture.class);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin/uiskin2/freezing-ui.json"));

        newGameButton = new TextButton("New Game", skin);
        loadGameButton = new TextButton("Load Game", skin);
        quitGameButton = new TextButton("Quit Game", skin);

        buttons = new TextButton[]{newGameButton, loadGameButton, quitGameButton};

        newGameButton.setPosition(861, 500);
        loadGameButton.setPosition(861, 400);
        quitGameButton.setPosition(861, 300);

        stage.addActor(newGameButton);
        stage.addActor(loadGameButton);
        stage.addActor(quitGameButton);

        music.setLooping(true);
        music.play();

        addClickListeners();
    }

    private void addClickListeners() {
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                music.stop();
                levelScreens.put("LevelSelectScreen", new LevelSelectScreen(game, levelScreens, savescreen));
                game.setScreen(levelScreens.get("LevelSelectScreen"));
            }
        });

        loadGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                music.stop();
                levelScreens.put("LoadScreen", new LoadScreen(game, levelScreens, savescreen));
                game.setScreen(levelScreens.get("LoadScreen"));
            }
        });

        quitGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleKeyboardInput();

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(logoTexture, 559, 600, 800, 400);
        batch.end();

        updateButtonSelection();

        stage.act(delta);
        stage.draw();
    }

    private void updateButtonSelection() {
        for (int i = 0; i < buttons.length; i++) {
            if (i == selectedButtonIndex) {
                buttons[i].setColor(1, 1, 0, 1);
            } else {
                buttons[i].setColor(1, 1, 1, 1);
            }
        }
    }

    private void handleKeyboardInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedButtonIndex = (selectedButtonIndex + 1) % buttons.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedButtonIndex = (selectedButtonIndex - 1 + buttons.length) % buttons.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            handleButtonClick();
        }
    }

    private void handleButtonClick() {
        if (selectedButtonIndex == 0) {
            music.stop();
            levelScreens.put("LevelSelectScreen", new LevelSelectScreen(game, levelScreens, savescreen));
            game.setScreen(levelScreens.get("LevelSelectScreen"));
        } else if (selectedButtonIndex == 1) {
            music.stop();
            levelScreens.put("LoadScreen", new LoadScreen(game, levelScreens, savescreen));
            game.setScreen(levelScreens.get("LoadScreen"));
        } else if (selectedButtonIndex == 2) {
            Gdx.app.exit();
        }
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
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}

