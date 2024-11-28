package com.AngryHomies.AngryBirds.assetManager;

import com.AngryHomies.AngryBirds.screens.FirstScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Graphics.DisplayMode;

import java.util.HashMap;

public class Main extends Game {
    public SpriteBatch batch;
    private HashMap<String, Screen> levelscreens;
    private HashMap<String, String> savedScreens;
    private AssetManager assetManager;
    @Override
    public void create() {

        batch = new SpriteBatch();
        levelscreens = new HashMap<>();
        savedScreens = new HashMap<>();
        assetManager = new AssetManager();


        savedScreens.put("SaveGame1Button", "");
        savedScreens.put("SaveGame2Button", "");
        savedScreens.put("SaveGame3Button", "");

        loadAssets();

        assetManager.finishLoading();

        levelscreens.put("FirstScreen",new FirstScreen(this,levelscreens,savedScreens));
        this.setScreen(levelscreens.get("FirstScreen"));


        DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        Gdx.graphics.setFullscreenMode(displayMode);
    }
    private void loadAssets() {

        assetManager.load("images/loading_screen.png", Texture.class);
        assetManager.load("images/load_bg.png", Texture.class);
        assetManager.load("images/angry_bird_image.png", Texture.class);
        assetManager.load("images/level_bg.png", Texture.class);
        assetManager.load("images/back_button.png", Texture.class);
        assetManager.load("images/big_pig.png", Texture.class);
        assetManager.load("images/black_bird.png", Texture.class);
        assetManager.load("images/block.png", Texture.class);
        assetManager.load("images/st_stoneblock.png", Texture.class);
        assetManager.load("images/sp_glassblock.png", Texture.class);
        assetManager.load("images/standing_woodblock.png", Texture.class);
        assetManager.load("images/catapult.png", Texture.class);
        assetManager.load("images/glass_block.png", Texture.class);
        assetManager.load("images/ground.png", Texture.class);
        assetManager.load("images/king_pig.png", Texture.class);
        assetManager.load("images/level_bg.png", Texture.class);
        assetManager.load("images/logo.png", Texture.class);
        assetManager.load("images/lose_screen.png", Texture.class);
        assetManager.load("images/lvl1.png", Texture.class);
        assetManager.load("images/lvl2.png", Texture.class);
        assetManager.load("images/lvl3.png", Texture.class);
        assetManager.load("images/lvl_select_logo.png", Texture.class);
        assetManager.load("images/no_loadgame.png", Texture.class);
        assetManager.load("images/pause_icon.png", Texture.class);
        assetManager.load("images/red_bird.png", Texture.class);
        assetManager.load("images/dot.png", Texture.class);
        assetManager.load("images/yellow_speed.png", Texture.class);
        assetManager.load("images/Black_bomb.png", Texture.class);
        assetManager.load("images/red_bird2.png", Texture.class);
        assetManager.load("images/red_attack.png", Texture.class);
        assetManager.load("images/sleepingwooden_block.png", Texture.class);
        assetManager.load("images/small_pig.png", Texture.class);
        //assetManager.load("images/stone_block.png", Texture.class);
        assetManager.load("images/win_screen.png", Texture.class);
        assetManager.load("images/yellow_bird.png", Texture.class);
//        assetManager.load("images/yellow_bird2.png", Texture.class);
        assetManager.load("images/success_screen.png", Texture.class);


        assetManager.load("audio/loading_music.mp3", Music.class);
        assetManager.load("audio/red_attack.mp3", Music.class);
        assetManager.load("audio/piglette oink a8.wav", Music.class);
    }

    @Override
    public void render() {
        super.render();
    }
    public AssetManager getAssetManager() {
        return assetManager;
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        assetManager.dispose();

    }

}

