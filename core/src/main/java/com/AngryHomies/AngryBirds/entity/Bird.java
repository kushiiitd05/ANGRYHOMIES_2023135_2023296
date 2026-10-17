package com.AngryHomies.AngryBirds.entity;

import com.AngryHomies.AngryBirds.entity.ability.AbilityContext;
import com.AngryHomies.AngryBirds.entity.ability.BirdAbility;
import com.AngryHomies.AngryBirds.gameplay.WorldConfig;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * The bird on (or launched from) the slingshot.
 *
 * <p>While aiming it is a kinematic body, so gravity can't drag it off the pouch. Launch makes it
 * dynamic with a velocity (not an impulse), which is what lets {@code TrajectoryPredictor} draw the
 * exact path. In flight it faces and stretches along its velocity; the first hit squashes it and
 * hands rotation back to physics.
 */
public final class Bird extends GameEntity {
    public enum State { LOADED, FLYING, LANDED }

    private final BirdType type;
    private final BirdAbility ability;
    private final TextureRegion abilityRegion;
    private State state = State.LOADED;
    private boolean abilityUsed;
    private float flightTime;
    private float slowTime;
    private float squash;
    private float facing;
    private final Vector2 velocity = new Vector2();

    public Bird(World world, BirdType type, Texture texture, Texture abilityTexture, float x, float y) {
        super(BodyFactory.circle(world, BodyDef.BodyType.KinematicBody, x, y, type.radius(), type.density(), 0.6f, 0.35f),
            new TextureRegion(texture), type.width(), type.height(), Float.MAX_VALUE);
        this.type = type;
        this.ability = type.newAbility();
        this.abilityRegion = new TextureRegion(abilityTexture);
    }

    public void moveTo(float x, float y) {
        body.setTransform(x, y, 0f);
        snapshot();
    }

    /** Fires the bird; continuous collision stops fast birds tunnelling through thin glass. */
    public void launch(Vector2 launchVelocity) {
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setBullet(true);
        body.setAngularDamping(1.2f);
        body.setFixedRotation(true);
        body.setLinearVelocity(new Vector2(launchVelocity).clamp(0f, WorldConfig.MAX_BIRD_SPEED));
        body.setAwake(true);
        state = State.FLYING;
    }

    /** Called every frame with the services abilities may need. */
    public void tick(float delta, AbilityContext context) {
        update(delta);
        squash = Math.max(0f, squash - delta * 5f);
        if (state == State.LOADED) {
            return;
        }
        flightTime += delta;
        velocity.set(body.getLinearVelocity());
        if (state == State.FLYING && velocity.len2() > 4f) {
            facing = velocity.angleDeg();
        }
        slowTime = velocity.len() < 1.2f ? slowTime + delta : 0f;
        ability.update(this, delta, context);
    }

    public boolean tryActivateAbility(AbilityContext context) {
        if (abilityUsed || state == State.LOADED || isDead()) {
            return false;
        }
        abilityUsed = true;
        region = abilityRegion;
        ability.activate(this, context);
        return true;
    }

    /** The first touch of anything ends the "arrow" flight pose. */
    public void onImpact(float speed) {
        if (state == State.FLYING) {
            state = State.LANDED;
            body.setFixedRotation(false);
        }
        squash = Math.min(1f, squash + speed / 40f);
    }

    /** Spent: stopped for a moment, or flying for too long, or gone off the level. */
    public boolean isFinished() {
        if (state == State.LOADED) {
            return false;
        }
        Vector2 p = body.getPosition();
        if (isDead() || WorldConfig.outOfBounds(p.x, p.y)) {
            return true;
        }
        return !ability.holdsBird(this) && (slowTime > 1.0f || flightTime > 12f);
    }

    @Override
    public boolean applyDamage(float amount) {
        return false; // birds are the hammer, not the nail
    }

    @Override
    protected float extraRotation() {
        // Flying: face along the path. After the first hit, physics owns the rotation.
        return (state == State.FLYING ? facing : 0f) + ability.spinDegrees();
    }

    @Override
    protected float scaleX() {
        float stretch = state == State.FLYING
            ? MathUtils.clamp(body.getLinearVelocity().len() / WorldConfig.MAX_BIRD_SPEED, 0f, 1f) * 0.18f : 0f;
        return 1f + stretch + squash * 0.25f;
    }

    @Override
    protected float scaleY() {
        float stretch = state == State.FLYING
            ? MathUtils.clamp(body.getLinearVelocity().len() / WorldConfig.MAX_BIRD_SPEED, 0f, 1f) * 0.1f : 0f;
        return 1f - stretch - squash * 0.2f + ability.pulse() * 0.15f;
    }

    @Override
    protected com.badlogic.gdx.graphics.Color tint() {
        com.badlogic.gdx.graphics.Color color = super.tint();
        float glow = ability.glow();
        return glow > 0f ? color.set(1f, 1f - glow * 0.6f, 1f - glow * 0.6f, 1f) : color;
    }

    @Override
    public float toughness() {
        return Float.MAX_VALUE;
    }

    public BirdType type() {
        return type;
    }

    public State state() {
        return state;
    }

    public boolean isAbilityUsed() {
        return abilityUsed;
    }

    /** Damage multiplier against a target: material speciality times any active move boost. */
    public float attackMultiplier(GameEntity target) {
        float speciality = target instanceof Block ? type.multiplierAgainst(((Block) target).material()) : 1f;
        return speciality * ability.attackBoost();
    }

    public float radius() {
        return type.radius();
    }
}
