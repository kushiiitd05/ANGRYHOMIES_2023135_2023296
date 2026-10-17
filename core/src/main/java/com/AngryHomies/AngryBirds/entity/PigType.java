package com.AngryHomies.AngryBirds.entity;

import com.AngryHomies.AngryBirds.assets.Assets;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;

/** Pig sizes. Bigger pigs are tougher: a falling plank flattens a small pig but only dents the king. */
public enum PigType {
    SMALL(Assets.SMALL_PIG, 40f, 1.0f, 5000),
    BIG(Assets.BIG_PIG, 70f, 2.2f, 5000),
    KING(Assets.KING_PIG, 110f, 3.0f, 10000);

    private final AssetDescriptor<Texture> texture;
    private final float health;
    private final float toughness;
    private final int score;

    PigType(AssetDescriptor<Texture> texture, float health, float toughness, int score) {
        this.texture = texture;
        this.health = health;
        this.toughness = toughness;
        this.score = score;
    }

    public AssetDescriptor<Texture> texture() {
        return texture;
    }

    public float health() {
        return health;
    }

    public float toughness() {
        return toughness;
    }

    public int score() {
        return score;
    }
}
