package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.graphics.Texture;

public abstract class Pig {
    protected Texture texture;
    protected float x, y, width, height;

    public Pig(Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
