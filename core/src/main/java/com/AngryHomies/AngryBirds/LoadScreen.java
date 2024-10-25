package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class LoadScreen implements Screen {

    private Main game;
    private Stage stage;
    private Texture backgroundTexture;
    private HashMap<String, Screen> levelScreens;
    private HashMap<String, String> savedScreens;

    private Texture loadScreenLogo;
    private Texture backButtonTexture;
    private Texture noLoadGameTexture;
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
//        noLoadGameTexture = game.getAssetManager().get("images/no_loadgame.png", Texture.class);

   
        backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.setPosition(30, 25);
        backButton.setSize(150, 150);
        stage.addActor(backButton);

        
        Texture saveGame1Texture = game.getAssetManager().get("images/lvl1.png", Texture.class);
        Texture saveGame2Texture = game.getAssetManager().get("images/lvl2.png", Texture.class);
        Texture saveGame3Texture = game.getAssetManager().get("images/lvl3.png", Texture.class);

        // Initialize class-level buttons
        saveGame1Button = new ImageButton(new TextureRegionDrawable(saveGame1Texture));
        saveGame2Button = new ImageButton(new TextureRegionDrawable(saveGame2Texture));
        saveGame3Button = new ImageButton(new TextureRegionDrawable(saveGame3Texture));

        // Set button sizes and positions
        saveGame1Button.getImageCell().width(150).height(150);
        saveGame2Button.getImageCell().width(150).height(150);
        saveGame3Button.getImageCell().width(150).height(150);

        saveGame1Button.setPosition(300, 700);
        saveGame2Button.setPosition(1079 - 218, 700);
        saveGame3Button.setPosition(1400, 700);

        // Add buttons to stage
        stage.addActor(saveGame1Button);
        stage.addActor(saveGame2Button);
        stage.addActor(saveGame3Button);

        // Add click listeners
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
                game.setScreen(new LevelOne(game, levelScreens, savedScreens));
            }
        });

        saveGame2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelOne(game, levelScreens, savedScreens));

            }
        });

        saveGame3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                  game.setScreen(new LevelOne(game, levelScreens, savedScreens));
            }
        });
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
        game.batch.draw(backgroundTexture, 0, 0, 650,500);
        game.batch.draw(loadScreenLogo, 1079 - 218, 600, 400, 150);  // Draw the logo
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
    public void dispose() {
       

    }
}
