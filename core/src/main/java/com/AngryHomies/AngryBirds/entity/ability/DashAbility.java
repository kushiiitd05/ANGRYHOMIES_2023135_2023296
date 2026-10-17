package com.AngryHomies.AngryBirds.entity.ability;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.gameplay.WorldConfig;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Yellow's speed dash: doubles speed along the current heading and leaves fading after-images.
 * The dash ignores gravity's curve for a moment because the new velocity is set, not added.
 */
public final class DashAbility implements BirdAbility {
    private static final float SPEED_FACTOR = 2.2f;
    private static final float MIN_DASH_SPEED = 70f;
    private static final float TRAIL_SECONDS = 0.6f;
    private static final float GHOST_INTERVAL = 0.03f;

    private float trail;
    private float ghostTimer;

    @Override
    public void activate(Bird bird, AbilityContext context) {
        Vector2 velocity = new Vector2(bird.body().getLinearVelocity());
        float speed = Math.min(Math.max(velocity.len() * SPEED_FACTOR, MIN_DASH_SPEED), WorldConfig.MAX_BIRD_SPEED);
        if (velocity.isZero()) {
            velocity.set(1f, 0f);
        }
        bird.body().setLinearVelocity(velocity.nor().scl(speed));
        context.play(Sfx.DASH);
        Vector2 p = bird.position();
        context.effects().ring(p.x, p.y, 10f, new Color(1f, 0.95f, 0.5f, 1f), 0.25f);
        context.effects().speedBurst(p.x, p.y, velocity.angleDeg());
        context.shake(0.15f);
        trail = TRAIL_SECONDS;
    }

    @Override
    public void update(Bird bird, float delta, AbilityContext context) {
        if (trail <= 0f) {
            return;
        }
        trail -= delta;
        ghostTimer -= delta;
        if (ghostTimer <= 0f && bird.state() == Bird.State.FLYING) {
            ghostTimer = GHOST_INTERVAL;
            Vector2 p = bird.position();
            context.effects().afterimage(bird.region(), p.x, p.y, bird.width(), bird.height(),
                bird.body().getLinearVelocity().angleDeg());
        }
    }

    @Override
    public float attackBoost() {
        return 1.6f; // a dashing bird hits harder for the rest of its flight
    }
}
