package com.AngryHomies.AngryBirds;

import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.assets.ProceduralTextures;
import com.AngryHomies.AngryBirds.audio.AudioManager;
import com.AngryHomies.AngryBirds.save.Progress;
import com.AngryHomies.AngryBirds.save.SaveManager;
import com.AngryHomies.AngryBirds.screen.LoadingScreen;
import com.AngryHomies.AngryBirds.screen.ScreenManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Application root. Owns the long-lived services (assets, audio, saves, progress) and hands
 * every frame to the {@link ScreenManager}, which runs screens and the transitions between them.
 */
public class Main extends ApplicationAdapter {
    private static final float MAX_FRAME_DELTA = 0.25f;

    private AssetManager assets;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private AudioManager audio;
    private ScreenManager screens;
    private Progress progress;
    private SaveManager saves;
    private ProceduralTextures textures;
    private Skin skin;

    @Override
    public void create() {
        assets = new AssetManager();
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        progress = new Progress(Gdx.app.getPreferences("angryhomies.progress"));
        saves = new SaveManager(Gdx.files.local("saves"));
        audio = new AudioManager(assets, progress.settings());
        screens = new ScreenManager(batch);
        applyDisplayMode();
        screens.set(new LoadingScreen(this), null);
    }

    /** Called once by the loading screen when every queued asset is in memory. */
    public void onAssetsLoaded() {
        skin = assets.get(Assets.SKIN);
        // The skin's fonts were made for a ~800px canvas; ours is 1920x1080 virtual pixels.
        skin.getFont("font").getData().setScale(2f);
        skin.getFont("button").getData().setScale(1.6f);
        skin.getFont("title").getData().setScale(1.5f);
        for (String name : new String[] {"font", "button", "title"}) {
            skin.getFont(name).getRegion().getTexture().setFilter(
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            skin.getFont(name).setUseIntegerPositions(false);
        }
        textures = new ProceduralTextures();
        audio.onAssetsLoaded();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_FRAME_DELTA);
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            progress.settings().setFullscreen(!progress.settings().isFullscreen());
            applyDisplayMode();
        }
        audio.update(delta);
        screens.render(delta);
        Demo.afterFrame(delta);
        DevLog.tick(delta);
    }

    public void applyDisplayMode() {
        if (progress.settings().isFullscreen()) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        } else if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1600, 900);
        }
    }

    @Override
    public void resize(int width, int height) {
        screens.resize(width, height);
    }

    @Override
    public void pause() {
        screens.pause();
    }

    @Override
    public void resume() {
        screens.resume();
    }

    @Override
    public void dispose() {
        screens.dispose();
        audio.dispose();
        if (textures != null) {
            textures.dispose();
        }
        shapes.dispose();
        batch.dispose();
        assets.dispose();
    }

    public AssetManager assets() {
        return assets;
    }

    public SpriteBatch batch() {
        return batch;
    }

    public ShapeRenderer shapes() {
        return shapes;
    }

    public AudioManager audio() {
        return audio;
    }

    public ScreenManager screens() {
        return screens;
    }

    public Progress progress() {
        return progress;
    }

    public SaveManager saves() {
        return saves;
    }

    public ProceduralTextures textures() {
        return textures;
    }

    public Skin skin() {
        return skin;
    }
}
