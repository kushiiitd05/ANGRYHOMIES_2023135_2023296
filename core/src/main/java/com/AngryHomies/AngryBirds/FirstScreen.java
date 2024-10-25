package com.AngryHomies.AngryBirds;

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
    private Main game;  // Reference to the Main game class
    private HashMap<String, Screen> levelScreens;
    private HashMap<String,String>savescreen;
    private AssetManager assetManager;

    private static final int VIRTUAL_WIDTH = 1920;   // Base width
    private static final int VIRTUAL_HEIGHT = 1080;  // Base height

    private String[] loadingStages = {"Loading", "Loading.", "Loading..", "Loading..."};
    private float loadingAnimationTime = 0f;
    private int currentStage = 0;

    // Constructor to pass the game instance
    public FirstScreen(Main game, HashMap<String, Screen> levelScreens, HashMap<String, String> savedScreens) {
        this.game = game;
        this.levelScreens = levelScreens;
        this.savescreen = savedScreens;
        this.assetManager = new AssetManager();


        // You can also switch to fullscreen dynamically
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    @Override
    public void show() {
        // Initialize SpriteBatch, Texture, and Music
        batch = new SpriteBatch();
        angryBirdTexture = game.getAssetManager().get("images/loading_screen.png", Texture.class);
        loadingMusic = game.getAssetManager().get("audio/loading_music.mp3", Music.class);
        loadingMusic.setLooping(true); // Loop the background music

        loadingMusic.play(); // Start playing music

        // Initialize font and scale it dynamically
        font = new BitmapFont();
        font.getData().setScale(3.0f); // Larger font size

        // Setup a viewport to handle resizing
        viewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        viewport.apply();

        timeElapsed = 0f; // Reset the timer
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0);

        // Update the time elapsed
        timeElapsed += delta;

        // Update the animation time and switch loading stages every 0.5 seconds
        loadingAnimationTime += delta;
        if (loadingAnimationTime > 0.25f) {
            currentStage = (currentStage + 1) % loadingStages.length;  // Cycle through stages
            loadingAnimationTime = 0f;  // Reset the animation timer
        }

        // Update viewport and camera
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        float imageX = 0;
        float imageY = 0;
        batch.draw(angryBirdTexture, imageX, imageY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Display the animated "Loading..." text at the bottom-right corner of the image
        float textX = imageX + angryBirdTexture.getWidth() - 250; // Align to bottom-right of the image
        float textY = imageY + 75 ; // Slightly above the bottom of the image
        font.draw(batch, loadingStages[currentStage], textX, textY);


        batch.end();

        // After 5-7 seconds, transition to the next screen
        if (timeElapsed > 2f) {
            // Switch to the home screen
            levelScreens.put("HomeScreen",new HomeScreen(game,loadingMusic,this.levelScreens,this.savescreen));
            game.setScreen(levelScreens.get("HomeScreen")); // Pass the game instance to HomeScreen
        }
    }

    @Override
    public void resize(int width, int height) {
        // Adjust the viewport on window resize
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Handle pausing of the screen
    }

    @Override
    public void resume() {
        // Handle resuming of the screen
    }

    @Override
    public void hide() {
        // This will be called when the screen is no longer visible
        dispose();
    }

    @Override
    public void dispose() {
        // Dispose only the SpriteBatch and Font, as AssetManager handles the rest

    }


}
   }
}
