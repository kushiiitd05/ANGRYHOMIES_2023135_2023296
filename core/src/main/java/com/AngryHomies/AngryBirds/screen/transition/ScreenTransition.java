package com.AngryHomies.AngryBirds.screen.transition;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Draws one frame of a screen change. The batch projection is a unit square (0..1 on both axes),
 * so implementations work in fractions of the screen and never care about resolution.
 */
public interface ScreenTransition {
    float duration();

    /** @param progress 0 at the start, 1 when the new screen fully replaces the old one */
    void render(SpriteBatch batch, TextureRegion from, TextureRegion to, float progress);
}
