package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;

public class SaveSuccessScreen implements Screen {
    private int x_pos;
    private int y_pos;
    private Texture background;
    private int width;
    private int height;
    private Main game;
    private SpriteBatch batch;
    private HashMap<String,Screen>levelscreen;
    private final float displayTime = 2f ;  // Display for 2 seconds
    private float goneTime = 0f;  // Track time

    public SaveSuccessScreen(Main game,HashMap<String,Screen>levelscreen ,Texture bg, int x_pos, int y_pos, int width, int height) {
        this.x_pos = x_pos;
        this.levelscreen = levelscreen;
        this.game = game;
        this.y_pos = y_pos;
        this.width = width;
        this.height = height;
        this.background = bg;
        this.batch = new SpriteBatch();
    }

    public Texture getBackground() {
        return background;
    }

    public void setBackground(Texture background) {
        this.background = background;
    }

    @Override
    public void show() {
        // Optional: Play a sound or trigger something when the screen shows.
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, x_pos, y_pos, width, height);
        batch.end();

        goneTime += delta;
        if (goneTime > displayTime) {
            game.setScreen(levelscreen.get("HomeScreen"));  // Return to the previous screen after 2 seconds
        }
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

     @Override
    public void hide() {
        /
        dispose();
    }

    @Override
    public void dispose() {
        

    }
}
