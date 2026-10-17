package com.AngryHomies.AngryBirds.entity.ability;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.gameplay.WorldConfig;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

/**
 * Red's move in two beats.
 *
 * <ol>
 *   <li><b>Whirlpool</b> (0.4 s): Red brakes in mid-air and spins; a vortex pulls nearby loose
 *       pieces in towards him while swirl particles circle.</li>
 *   <li><b>Blade strike</b>: he cuts forward along his original heading. Everything the cut line
 *       passes through takes heavy damage and is knocked sideways, a white slash streak flashes,
 *       and Red rockets along the cut.</li>
 * </ol>
 */
public final class WhirlBladeAbility implements BirdAbility {
    private static final float WHIRL_SECONDS = 0.4f;
    private static final float VORTEX_RADIUS = 20f;
    private static final float VORTEX_STRENGTH = 900f;
    private static final float BLADE_LENGTH = 48f;
    private static final float BLADE_DAMAGE = 150f;
    private static final float STRIKE_SPEED = 75f;

    private enum Phase { IDLE, WHIRL, DONE }

    private Phase phase = Phase.IDLE;
    private float timer;
    private float spin;
    private float swirlTimer;
    private final Vector2 heading = new Vector2(1f, 0f);
    private float strikeBoost;

    @Override
    public void activate(Bird bird, AbilityContext context) {
        heading.set(bird.body().getLinearVelocity()).nor();
        if (heading.isZero()) {
            heading.set(1f, 0f);
        }
        bird.body().setLinearVelocity(bird.body().getLinearVelocity().scl(0.15f));
        bird.body().setGravityScale(0.1f);
        phase = Phase.WHIRL;
        timer = WHIRL_SECONDS;
        context.play(Sfx.WHIRL);
        context.play(Sfx.BATTLE_CRY);
    }

    @Override
    public void update(Bird bird, float delta, AbilityContext context) {
        strikeBoost = Math.max(0f, strikeBoost - delta);
        if (phase != Phase.WHIRL) {
            return;
        }
        timer -= delta;
        float progress = 1f - timer / WHIRL_SECONDS;
        spin += delta * (720f + 2160f * Interpolation.pow2In.apply(progress)); // spins up
        Vector2 center = bird.position();
        context.vortex(bird, center, VORTEX_RADIUS, VORTEX_STRENGTH * delta);
        swirlTimer -= delta;
        if (swirlTimer <= 0f) {
            swirlTimer = 0.04f;
            context.effects().swirl(center.x, center.y, VORTEX_RADIUS * (1f - progress * 0.5f), spin);
        }
        if (timer <= 0f) {
            strike(bird, context);
        }
    }

    private void strike(Bird bird, AbilityContext context) {
        phase = Phase.DONE;
        spin = 0f;
        Vector2 from = new Vector2(bird.position());
        Vector2 to = new Vector2(heading).scl(BLADE_LENGTH).add(from);
        context.bladeStrike(bird, from, to, BLADE_DAMAGE);
        context.effects().slash(from.x, from.y, to.x, to.y);
        context.play(Sfx.SLASH);
        context.shake(0.35f);
        context.hitStop(0.05f);
        bird.body().setGravityScale(1f);
        bird.body().setLinearVelocity(new Vector2(heading).scl(Math.min(STRIKE_SPEED, WorldConfig.MAX_BIRD_SPEED)));
        strikeBoost = 1f;
    }

    @Override
    public float spinDegrees() {
        return phase == Phase.WHIRL ? spin : 0f;
    }

    @Override
    public float attackBoost() {
        return strikeBoost > 0f ? 1.6f : 1f;
    }
}
