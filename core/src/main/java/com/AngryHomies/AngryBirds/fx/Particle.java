package com.AngryHomies.AngryBirds.fx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

/** One pooled particle: position, motion, size over life and colour. World units throughout. */
public final class Particle implements Pool.Poolable {
    TextureRegion region;
    float x;
    float y;
    float vx;
    float vy;
    float gravity;
    float drag;
    float life;
    float maxLife;
    float startSize;
    float endSize;
    float stretch = 1f;
    float rotation;
    float spin;
    boolean additive;
    final Color color = new Color();

    Particle set(TextureRegion region, float x, float y, float vx, float vy, float life,
                 float startSize, float endSize, Color color) {
        this.region = region;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.startSize = startSize;
        this.endSize = endSize;
        this.color.set(color);
        return this;
    }

    /** @return false once the particle has expired */
    boolean update(float delta) {
        life -= delta;
        if (life <= 0f) {
            return false;
        }
        float damping = 1f / (1f + drag * delta);
        vx *= damping;
        vy = (vy + gravity * delta) * damping;
        x += vx * delta;
        y += vy * delta;
        rotation += spin * delta;
        return true;
    }

    float progress() {
        return 1f - life / maxLife;
    }

    @Override
    public void reset() {
        region = null;
        gravity = 0f;
        drag = 0f;
        stretch = 1f;
        rotation = 0f;
        spin = 0f;
        additive = false;
    }
}
