package com.AngryHomies.AngryBirds.screens;

import com.AngryHomies.AngryBirds.assetManager.Main;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;

public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private Texture angryBirdTexture;
    private Music loadingMusic;
    private BitmapFont font;
    private Viewport viewport;
    private float timeElapsed;
    private Main game;
    private HashMap<String, Screen> levelScreens;
    private HashMap<String, String> savescreen;
    private AssetManager assetManager;

    private static final int VIRTUAL_WIDTH = 1920;
    private static final int VIRTUAL_HEIGHT = 1080;

    private String[] loadingStages = {"Loading", "Loading.", "Loading..", "Loading..."};
    private float loadingAnimationTime = 0f;
    private int currentStage = 0;

    public FirstScreen(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savedScreens) {
        this.game = game;
        this.levelScreens = levelScreens;
        this.savescreen = savedScreens;
        this.assetManager = new AssetManager();
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        angryBirdTexture = game.getAssetManager().get("images/loading_screen.png", Texture.class);
        loadingMusic = game.getAssetManager().get("audio/loading_music.mp3", Music.class);
        loadingMusic.setLooping(true);
        loadingMusic.play();
        font = new BitmapFont();
        font.getData().setScale(3.0f);

        viewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        viewport.apply();

        timeElapsed = 0f;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0);

        timeElapsed += delta;
        loadingAnimationTime += delta;
        if (loadingAnimationTime > 0.25f) {
            currentStage = (currentStage + 1) % loadingStages.length;
            loadingAnimationTime = 0f;
        }
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        float imageX = 0;
        float imageY = 0;
        batch.draw(angryBirdTexture, imageX, imageY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        float textX = imageX + angryBirdTexture.getWidth() - 250;
        float textY = imageY + 75;
        font.draw(batch, loadingStages[currentStage], textX, textY);

        batch.end();

        if (timeElapsed > 8f) {
            levelScreens.put("HomeScreen", new HomeScreen(game, loadingMusic, this.levelScreens, this.savescreen));
            game.setScreen(levelScreens.get("HomeScreen"));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
    }
}

