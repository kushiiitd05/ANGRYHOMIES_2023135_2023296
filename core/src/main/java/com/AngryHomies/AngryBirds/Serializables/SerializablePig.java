package com.AngryHomies.AngryBirds.Serializables;

import com.AngryHomies.AngryBirds.entities.pig.Pig;

import java.io.Serializable;

public class SerializablePig implements Serializable {
    private String id;
    private float x,y;
    private float width;
    private float height;
    private float density;
    private float diameter;
    private float health;
    private boolean isDefeated;
    private SerializableBody bodyState;

    public SerializablePig(Pig pig){
        this.id = pig.getId();
        this.x = pig.getX();
        this.y = pig.getY();
        this.width = pig.getWidth();
        this.height = pig.getHeight();
        this.density = pig.getDensity();
        this.diameter = pig.getDiameter();
        this.health = pig.getHealth();
        this.isDefeated = pig.isDefeated();

        if (pig.getBody() != null) {
            this.bodyState = new SerializableBody(pig.getBody());
        }
    }

    public float getX() {
        return x;
    }

    public String getId() {
        return id;
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

    public float getDiameter() {
        return diameter;
    }

    public float getHealth() {
        return health;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public SerializableBody getBodyState() {
        return bodyState;
    }
}

