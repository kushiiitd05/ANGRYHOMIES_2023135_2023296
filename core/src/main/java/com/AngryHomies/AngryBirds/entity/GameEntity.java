package com.AngryHomies.AngryBirds.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Anything with a physics body and a sprite: birds, pigs and blocks.
 *
 * <p>Keeps the transform from the previous physics step so rendering can interpolate smoothly
 * between fixed steps, and handles shared feedback: a white hit flash and damage darkening.
 */
public abstract class GameEntity {
    private static final float FLASH_SECONDS = 0.09f;
    private static final Color TINT = new Color();
    private static int nextId;

    /** Unique per entity, for logs and debugging. */
    public final int id = ++nextId;

    protected final Body body;
    protected TextureRegion region;
    protected final float width;
    protected final float height;
    protected float health;
    protected final float maxHealth;
    private final Vector2 previous = new Vector2();
    private float previousAngle;
    private float flash;
    private float hitShake;
    private boolean dead;
    private final Vector2 lastHit = new Vector2();
    protected float age;

    protected GameEntity(Body body, TextureRegion region, float width, float height, float health) {
        this.body = body;
        this.region = region;
        this.width = width;
        this.height = height;
        this.health = health;
        this.maxHealth = health;
        body.setUserData(this);
        snapshot();
        lastHit.set(body.getPosition());
    }

    /** Remember this step's transform; called before every physics step. */
    public void snapshot() {
        previous.set(body.getPosition());
        previousAngle = body.getAngle();
    }

    public void update(float delta) {
        age += delta;
        flash = Math.max(0f, flash - delta);
        hitShake = Math.max(0f, hitShake - delta);
    }

    /** @return true if this hit destroyed the entity */
    public boolean applyDamage(float amount) {
        if (dead || amount <= 0f) {
            return false;
        }
        health -= amount;
        flash = FLASH_SECONDS;
        hitShake = Math.min(0.3f, hitShake + amount / maxHealth * 0.6f);
        if (health <= 0f) {
            dead = true;
            return true;
        }
        return false;
    }

    /** Where the most recent hit landed (world coordinates). */
    public void markHit(float x, float y) {
        lastHit.set(x, y);
    }

    public Vector2 lastHit() {
        return lastHit;
    }

    public void kill() {
        dead = true;
    }

    public void render(SpriteBatch batch, float alpha) {
        float x = renderX(alpha);
        float y = renderY(alpha);
        Color old = batch.getColor().cpy();
        batch.setColor(tint());
        batch.draw(region, x - width / 2f, y - height / 2f + offsetY(), width / 2f, height / 2f,
            width, height, scaleX(), scaleY(), renderAngle(alpha) + extraRotation());
        batch.setColor(old);
    }

    /** Interpolated position between the last two physics steps (see PhysicsWorld). */
    public float renderX(float alpha) {
        return previous.x + (body.getPosition().x - previous.x) * alpha;
    }

    public float renderY(float alpha) {
        return previous.y + (body.getPosition().y - previous.y) * alpha;
    }

    /** Interpolated angle in degrees. */
    public float renderAngle(float alpha) {
        return (previousAngle + (body.getAngle() - previousAngle) * alpha) * MathUtils.radiansToDegrees;
    }

    /** Darker as health drops; white flash right after a hit. */
    protected Color tint() {
        float shade = 0.62f + 0.38f * healthRatio();
        TINT.set(shade, shade, shade, 1f);
        if (flash > 0f) {
            float f = flash / FLASH_SECONDS;
            TINT.lerp(Color.WHITE, f).add(f * 0.4f, f * 0.4f, f * 0.4f, 0f);
        }
        return TINT;
    }

    protected float scaleX() {
        return 1f;
    }

    protected float scaleY() {
        return 1f;
    }

    protected float offsetY() {
        return 0f;
    }

    /** A short decaying wobble after each hit sells the impact. */
    protected float extraRotation() {
        return hitShake > 0f ? MathUtils.sin(age * 70f) * hitShake * 10f : 0f;
    }

    /** Distance from {@code point} to this entity's surface (0 if inside). Circle by default. */
    public float surfaceDistance(Vector2 point) {
        return Math.max(0f, body.getPosition().dst(point) - Math.min(width, height) / 2f);
    }

    /** How easily this entity is damaged; higher is tougher. */
    public abstract float toughness();

    public float healthRatio() {
        return MathUtils.clamp(health / maxHealth, 0f, 1f);
    }

    public Body body() {
        return body;
    }

    public Vector2 position() {
        return body.getPosition();
    }

    public float health() {
        return health;
    }

    public boolean isDead() {
        return dead;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public TextureRegion region() {
        return region;
    }
}
