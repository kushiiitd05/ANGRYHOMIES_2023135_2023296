package com.AngryHomies.AngryBirds.screen.transition;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;

/** Pushes the old screen out sideways while the new one slides in after it. */
public final class SlideTransition implements ScreenTransition {
    public enum Direction { LEFT, RIGHT }

    private final Direction direction;
    private final float duration;

    public SlideTransition(Direction direction) {
        this(direction, 0.55f);
    }

    public SlideTransition(Direction direction, float duration) {
        this.direction = direction;
        this.duration = duration;
    }

    @Override
    public float duration() {
        return duration;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion from, TextureRegion to, float progress) {
        float offset = Interpolation.pow3Out.apply(progress);
        float sign = direction == Direction.LEFT ? -1f : 1f;
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(from, sign * offset, 0f, 1f, 1f);
        batch.draw(to, sign * (offset - 1f), 0f, 1f, 1f);
    }
}
