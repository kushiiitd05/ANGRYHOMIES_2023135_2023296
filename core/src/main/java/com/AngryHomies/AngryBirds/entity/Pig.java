package com.AngryHomies.AngryBirds.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

/** A pig. Breathes while idle and squashes when hit, so it never looks like a static sticker. */
public final class Pig extends GameEntity {
    private final PigType type;
    private final float radius;
    private final float density;
    private final float breathPhase = MathUtils.random(MathUtils.PI2);
    private float squash;

    public Pig(World world, PigType type, Texture texture, float x, float y, float width, float height,
               float radius, float density) {
        super(BodyFactory.circle(world, BodyDef.BodyType.DynamicBody, x, y, radius, density, 0.8f, 0.2f),
            new TextureRegion(texture), width, height, type.health());
        this.type = type;
        this.radius = radius;
        this.density = density;
        body.setAngularDamping(1.5f);
    }

    public Pig withHealth(float remaining) {
        health = Math.min(remaining, maxHealth);
        return this;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        squash = Math.max(0f, squash - delta * 4f);
    }

    @Override
    public boolean applyDamage(float amount) {
        squash = Math.min(1f, squash + 0.6f);
        return super.applyDamage(amount);
    }

    @Override
    protected float scaleX() {
        return 1f + squash * 0.18f;
    }

    @Override
    protected float scaleY() {
        float breath = 1f + MathUtils.sin(age * 2.6f + breathPhase) * 0.025f;
        return breath - squash * 0.16f;
    }

    @Override
    public float surfaceDistance(com.badlogic.gdx.math.Vector2 point) {
        return Math.max(0f, body.getPosition().dst(point) - radius);
    }

    @Override
    public float toughness() {
        return type.toughness();
    }

    public PigType type() {
        return type;
    }

    public float radius() {
        return radius;
    }

    public float density() {
        return density;
    }
}
