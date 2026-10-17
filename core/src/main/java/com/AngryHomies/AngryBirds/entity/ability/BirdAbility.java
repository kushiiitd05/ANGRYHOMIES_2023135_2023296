package com.AngryHomies.AngryBirds.entity.ability;

import com.AngryHomies.AngryBirds.entity.Bird;

/**
 * A bird's special move (strategy pattern), triggered once per flight by a click or Space.
 *
 * <p>Each move is a small timed sequence of physics (impulses, velocity), visuals (texture swap,
 * particles, glow, shake) and sound. {@link #update} runs every frame after activation so moves
 * can have a wind-up, like the black bird's fuse.
 */
public interface BirdAbility {
    void activate(Bird bird, AbilityContext context);

    default void update(Bird bird, float delta, AbilityContext context) {
    }

    /** Extra damage multiplier while the move is active. */
    default float attackBoost() {
        return 1f;
    }

    /** 0..1 red glow for wind-up effects. */
    default float glow() {
        return 0f;
    }

    /** 0..1 scale pulse for wind-up effects. */
    default float pulse() {
        return 0f;
    }

    /** True while the move still needs the bird in play (e.g. a bomb waiting to go off). */
    default boolean holdsBird(Bird bird) {
        return false;
    }

    /** Extra sprite rotation, e.g. Red spinning up during the whirlpool. */
    default float spinDegrees() {
        return 0f;
    }
}
