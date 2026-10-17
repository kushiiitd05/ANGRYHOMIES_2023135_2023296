package com.AngryHomies.AngryBirds.fx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/** A few soft clouds drifting across the sky at different speeds (cheap parallax). */
public final class Clouds {
    private static final int COUNT = 6;

    private final Texture texture;
    private final float[] x = new float[COUNT];
    private final float[] y = new float[COUNT];
    private final float[] width = new float[COUNT];
    private final float[] speed = new float[COUNT];
    private final float left;
    private final float right;

    public Clouds(Texture texture, float left, float right, float minY, float maxY) {
        this.texture = texture;
        this.left = left;
        this.right = right;
        for (int i = 0; i < COUNT; i++) {
            x[i] = MathUtils.random(left, right);
            y[i] = MathUtils.random(minY, maxY);
            width[i] = MathUtils.random(34f, 70f);
            speed[i] = MathUtils.random(0.8f, 2.6f) * (width[i] / 70f); // bigger = closer = faster
        }
    }

    public void update(float delta) {
        for (int i = 0; i < COUNT; i++) {
            x[i] += speed[i] * delta;
            if (x[i] - width[i] > right) {
                x[i] = left - width[i];
            }
        }
    }

    public void render(SpriteBatch batch) {
        Color old = batch.getColor().cpy();
        for (int i = 0; i < COUNT; i++) {
            batch.setColor(1f, 1f, 1f, 0.35f + width[i] / 200f);
            batch.draw(texture, x[i] - width[i] / 2f, y[i], width[i], width[i] / 2f);
        }
        batch.setColor(old);
    }
}
