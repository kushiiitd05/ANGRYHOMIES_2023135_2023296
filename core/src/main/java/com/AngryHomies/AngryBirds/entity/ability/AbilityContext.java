package com.AngryHomies.AngryBirds.entity.ability;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.AngryHomies.AngryBirds.entity.Bird;
import com.AngryHomies.AngryBirds.fx.Effects;
import com.badlogic.gdx.math.Vector2;

/** What a special move may touch in the level, without depending on the whole gameplay screen. */
public interface AbilityContext {
    void play(Sfx sfx);

    /** Adds camera shake (0..1 trauma). */
    void shake(float trauma);

    /** Freezes physics for a few frames so a big hit lands with weight. */
    void hitStop(float seconds);

    Effects effects();

    /** Radial blast: impulse and damage fall off as (1 - d/r)^2 from the centre. */
    void explode(Bird source, Vector2 center, float radius, float impulse, float damage);

    /** Pulls loose bodies within {@code radius} towards {@code center} (call every frame). */
    void vortex(Bird source, Vector2 center, float radius, float impulsePerFrame);

    /** Damages and knocks aside everything the segment {@code from -> to} passes through. */
    void bladeStrike(Bird source, Vector2 from, Vector2 to, float damage);

    /** Removes the bird (e.g. after it exploded). */
    void removeBird(Bird bird);
}
