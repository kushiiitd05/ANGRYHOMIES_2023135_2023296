package com.AngryHomies.AngryBirds.entity.ability;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Black's bomb: a short fuse (hiss, red throb), then a radial blast that damages and throws
 * everything nearby. Like the original, he also blows up by himself shortly after landing.
 */
public final class BombAbility implements BirdAbility {
    private static final float FUSE_SECONDS = 0.45f;
    private static final float AUTO_FUSE_AFTER_LANDING = 1.6f;
    private static final float RADIUS = 26f;
    private static final float IMPULSE = 6800f;
    private static final float DAMAGE = 220f;

    private float fuse = -1f;
    private float landedFor;
    private boolean exploded;

    @Override
    public void activate(Bird bird, AbilityContext context) {
        fuse = FUSE_SECONDS;
        context.play(Sfx.FUSE);
    }

    @Override
    public void update(Bird bird, float delta, AbilityContext context) {
        if (exploded) {
            return;
        }
        if (fuse < 0f) {
            landedFor = bird.state() == Bird.State.LANDED ? landedFor + delta : 0f;
            if (landedFor > AUTO_FUSE_AFTER_LANDING) {
                bird.tryActivateAbility(context);
            }
            return;
        }
        fuse -= delta;
        if (fuse <= 0f) {
            exploded = true;
            Vector2 center = new Vector2(bird.position());
            context.play(Sfx.EXPLOSION);
            context.explode(bird, center, RADIUS, IMPULSE, DAMAGE);
            context.effects().explosion(center.x, center.y, RADIUS);
            context.shake(0.75f);
            context.hitStop(0.07f);
            context.removeBird(bird);
        }
    }

    /** A black bird always ends in a bang: keep it in play until it explodes. */
    @Override
    public boolean holdsBird(Bird bird) {
        return !exploded;
    }

    @Override
    public float glow() {
        return fuse >= 0f && !exploded ? 0.5f + 0.5f * MathUtils.sin((FUSE_SECONDS - fuse) * 40f) : 0f;
    }

    @Override
    public float pulse() {
        return fuse >= 0f && !exploded ? (FUSE_SECONDS - fuse) / FUSE_SECONDS : 0f;
    }
}
