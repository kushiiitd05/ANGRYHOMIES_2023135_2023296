package com.AngryHomies.AngryBirds.Serializables;

import com.AngryHomies.AngryBirds.entities.bird.Bird;

import java.io.Serializable;

public class SerializableBird implements Serializable {


    private String id;
    private float x,y;
    private float width;
    private float height;
    private float density;
    private float diameter;
    private boolean isLaunched;
    private float launchTime;
    private float attackPower;
    private SerializableBody bodyState;

    public SerializableBird(Bird bird){
        this.id = bird.getId();
        this.x = bird.getX();
        this.y = bird.getY();
        this.width = bird.getWidth();
        this.height = bird.getHeight();
        this.density = bird.getDensity();
        this.diameter = bird.getDiameter();
        this.isLaunched = bird.isLaunched();
        this.launchTime = bird.getLaunchTime();
        this.attackPower = bird.getAttackPower();
        if (bird.getBody() != null) {
            this.bodyState = new SerializableBody(bird.getBody());
        }
    }

    public float getDiameter() {
        return diameter;
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

    public float getAttackPower() {
        return height;
    }

    public SerializableBody getBodyState() {
        return bodyState;
    }

    public float getDensity() {
        return density;
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    public float getLaunchTime() {
        return launchTime;
    }

}

