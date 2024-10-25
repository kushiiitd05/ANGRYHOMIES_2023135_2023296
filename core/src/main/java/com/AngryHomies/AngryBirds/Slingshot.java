package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.graphics.Texture;

public class Slingshot {
    private Texture slingshot_texture;
    private int x_pos;
    private int y_pos;
    private int width;
    private int height;
    public Slingshot(Texture slingshot_texture, int x_pos, int y_pos, int width, int height) {
        this.slingshot_texture = slingshot_texture;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.width = width;
        this.height = height;
    }
    public Texture getSlingshot_texture() {
        return slingshot_texture;
    }
    public void setSlingshot_texture(Texture slingshot_texture) {
        this.slingshot_texture = slingshot_texture;
    }
    public int getx_pos() {
        return x_pos;
    }
    public void setx_pos(int x_pos) {
        this.x_pos = x_pos;
    }
    public int gety_pos() {
        return y_pos;
    }
    public void sety_pos(int y_pos) {
        this.y_pos = y_pos;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }


}

