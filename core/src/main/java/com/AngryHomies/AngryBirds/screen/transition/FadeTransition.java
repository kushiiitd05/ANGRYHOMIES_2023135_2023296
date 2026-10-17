package com.AngryHomies.AngryBirds.screen.transition;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;

/** Dips to black and back: the old screen darkens, then the new one brightens. */
public final class FadeTransition implements ScreenTransition {
    private final float duration;

    public FadeTransition() {
        this(0.6f);
    }

    public FadeTransition(float duration) {
        this.duration = duration;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion from, TextureRegion to, float progress) {
        boolean firstHalf = progress < 0.5f;
        float brightness = Interpolation.fade.apply(firstHalf ? 1f - progress * 2f : progress * 2f - 1f);
        batch.setColor(brightness, brightness, brightness, 1f);
        batch.draw(firstHalf ? from : to, 0f, 0f, 1f, 1f);
    }
}
