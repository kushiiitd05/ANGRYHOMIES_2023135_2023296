package com.AngryHomies.AngryBirds.entity;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.badlogic.gdx.graphics.Color;

/** What a block is made of: how strong it is, how it sounds and what its debris looks like. */
public enum Material {
    WOOD(55f, 1.0f, 0.7f, 0.12f, 500, Sfx.WOOD_HIT, Sfx.WOOD_BREAK, new Color(0.72f, 0.48f, 0.24f, 1f)),
    GLASS(28f, 0.55f, 0.35f, 0.08f, 300, Sfx.GLASS_HIT, Sfx.GLASS_BREAK, new Color(0.72f, 0.9f, 1f, 1f)),
    STONE(110f, 2.2f, 0.9f, 0.05f, 800, Sfx.STONE_HIT, Sfx.STONE_BREAK, new Color(0.62f, 0.62f, 0.66f, 1f));

    private final float baseHealth;
    private final float toughness;
    private final float friction;
    private final float restitution;
    private final int score;
    private final Sfx hitSound;
    private final Sfx breakSound;
    private final Color debrisColor;

    Material(float baseHealth, float toughness, float friction, float restitution, int score,
             Sfx hitSound, Sfx breakSound, Color debrisColor) {
        this.baseHealth = baseHealth;
        this.toughness = toughness;
        this.friction = friction;
        this.restitution = restitution;
        this.score = score;
        this.hitSound = hitSound;
        this.breakSound = breakSound;
        this.debrisColor = debrisColor;
    }

    /** Bigger pieces take more punishment: health grows with the square root of the area. */
    public float healthFor(float width, float height) {
        return baseHealth * (0.5f + (float) Math.sqrt(width * height) / 8f);
    }

    public float toughness() {
        return toughness;
    }

    public float friction() {
        return friction;
    }

    public float restitution() {
        return restitution;
    }

    public int score() {
        return score;
    }

    public Sfx hitSound() {
        return hitSound;
    }

    public Sfx breakSound() {
        return breakSound;
    }

    public Color debrisColor() {
        return debrisColor;
    }
}
