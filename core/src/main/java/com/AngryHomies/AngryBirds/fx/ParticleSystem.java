package com.AngryHomies.AngryBirds.fx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Updates and draws particles from a pool, so explosions don't allocate. Normal particles (dust,
 * debris, smoke) draw first; additive ones (fire, flashes) draw on top and brighten what's behind.
 */
public final class ParticleSystem {
    private static final int MAX_PARTICLES = 900;

    private final Pool<Particle> pool = Pools.get(Particle.class, MAX_PARTICLES);
    private final Array<Particle> active = new Array<>(false, 256);
    private final Color drawColor = new Color();

    public Particle spawn(TextureRegion region, float x, float y, float vx, float vy, float life,
                          float startSize, float endSize, Color color) {
        if (active.size >= MAX_PARTICLES) {
            pool.free(active.removeIndex(0));
        }
        Particle particle = pool.obtain().set(region, x, y, vx, vy, life, startSize, endSize, color);
        active.add(particle);
        return particle;
    }

    public void update(float delta) {
        for (int i = active.size - 1; i >= 0; i--) {
            Particle particle = active.get(i);
            if (!particle.update(delta)) {
                active.removeIndex(i);
                pool.free(particle);
            }
        }
    }

    public void render(SpriteBatch batch) {
        Color old = batch.getColor().cpy();
        drawPass(batch, false);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawPass(batch, true);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(old);
    }

    private void drawPass(SpriteBatch batch, boolean additive) {
        for (Particle p : active) {
            if (p.additive != additive) {
                continue;
            }
            float t = p.progress();
            float size = p.startSize + (p.endSize - p.startSize) * t;
            float fade = t < 0.7f ? 1f : 1f - (t - 0.7f) / 0.3f;
            drawColor.set(p.color).a = p.color.a * fade;
            batch.setColor(drawColor);
            float w = size * p.stretch;
            batch.draw(p.region, p.x - w / 2f, p.y - size / 2f, w / 2f, size / 2f, w, size, 1f, 1f, p.rotation);
        }
    }

    public int count() {
        return active.size;
    }

    public void clear() {
        pool.freeAll(active);
        active.clear();
    }
}
