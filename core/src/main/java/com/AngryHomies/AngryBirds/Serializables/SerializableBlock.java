package com.AngryHomies.AngryBirds.Serializables;

import com.AngryHomies.AngryBirds.entities.block.Block;

import java.io.Serializable;

public class SerializableBlock implements Serializable {

    private String id;
    private float x,y;
    private float width;
    private float height;
    private float density;
    private String orientation;
    private float health;
    private boolean isDestroyed;
    private boolean onGround;
    private SerializableBody bodyState;
    private String texturePath;

    public SerializableBlock(Block block){

        this.id = block.getId();
        this.x = block.getX();
        this.y = block.getY();
        this.width = block.getWidth();
        this.height = block.getHeight();
        this.density = block.getDensity();
        this.isDestroyed = block.isDestroyed();
        this.health = block.getHealth();
        this.onGround = block.isOnGround();
        this.texturePath = block.getTexture().toString();
        this.orientation = block.getWidth() > block.getHeight() ? "sleeping" : "standing";

        if (block.getBody() != null) {
            this.bodyState = new SerializableBody(block.getBody());
        }
    }

    public String getId() {
        return id;
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

    public float getDensity() {
        return density;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public float getHealth() {
        return health;
    }

    public boolean isOnGround() {
        return onGround;
    }
    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
    public SerializableBody getBodyState() {
        return bodyState;
    }
    public String getTexturePath() {
        return texturePath;
    }
}
