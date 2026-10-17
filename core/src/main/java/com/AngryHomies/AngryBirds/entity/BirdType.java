package com.AngryHomies.AngryBirds.entity;

import com.AngryHomies.AngryBirds.assets.Assets;
import com.AngryHomies.AngryBirds.entity.ability.BirdAbility;
import com.AngryHomies.AngryBirds.entity.ability.BombAbility;
import com.AngryHomies.AngryBirds.entity.ability.DashAbility;
import com.AngryHomies.AngryBirds.entity.ability.WhirlBladeAbility;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

/**
 * The three birds. Sizes and densities come from the original game classes; the material
 * multipliers give each bird a speciality (yellow shreds wood, black smashes stone).
 */
public enum BirdType {
    RED(Assets.RED_BIRD, Assets.RED_ATTACK, 7.5f, 7.5f, 2.67f, 3.9f, 1f, 1f, 1f,
        new Color(0.86f, 0.1f, 0.12f, 1f)),
    YELLOW(Assets.YELLOW_BIRD, Assets.YELLOW_SPEED, 8.5f, 8.5f, 3.33f, 2.5f, 2.0f, 1.1f, 0.6f,
        new Color(1f, 0.84f, 0.1f, 1f)),
    BLACK(Assets.BLACK_BIRD, Assets.BLACK_BOMB, 9.5f, 9f, 4.17f, 1.7f, 1.1f, 1f, 2.5f,
        new Color(0.16f, 0.16f, 0.2f, 1f));

    private final AssetDescriptor<Texture> texture;
    private final AssetDescriptor<Texture> abilityTexture;
    private final float width;
    private final float height;
    private final float radius;
    private final float density;
    private final float woodMultiplier;
    private final float glassMultiplier;
    private final float stoneMultiplier;
    private final Color featherColor;

    BirdType(AssetDescriptor<Texture> texture, AssetDescriptor<Texture> abilityTexture, float width, float height,
             float radius, float density, float wood, float glass, float stone, Color featherColor) {
        this.texture = texture;
        this.abilityTexture = abilityTexture;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.density = density;
        this.woodMultiplier = wood;
        this.glassMultiplier = glass;
        this.stoneMultiplier = stone;
        this.featherColor = featherColor;
    }

    public BirdAbility newAbility() {
        switch (this) {
            case YELLOW:
                return new DashAbility();
            case BLACK:
                return new BombAbility();
            default:
                return new WhirlBladeAbility();
        }
    }

    public float multiplierAgainst(Material material) {
        switch (material) {
            case WOOD:
                return woodMultiplier;
            case GLASS:
                return glassMultiplier;
            default:
                return stoneMultiplier;
        }
    }

    public AssetDescriptor<Texture> texture() {
        return texture;
    }

    public AssetDescriptor<Texture> abilityTexture() {
        return abilityTexture;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float radius() {
        return radius;
    }

    public float density() {
        return density;
    }

    public Color featherColor() {
        return featherColor;
    }
}
