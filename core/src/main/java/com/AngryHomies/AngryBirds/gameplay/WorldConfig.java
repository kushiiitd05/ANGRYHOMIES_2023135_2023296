package com.AngryHomies.AngryBirds.gameplay;

/**
 * World constants. One world unit is one metre, which is 10 pixels of the original 1920x1080
 * layout, so every coordinate from the old levels carries over unchanged.
 */
public final class WorldConfig {
    public static final float WIDTH = 192f;
    public static final float HEIGHT = 108f;
    public static final float GROUND_Y = 15.5f;
    /** Stronger than the old -8 so flight arcs and falling towers feel snappy, not floaty. */
    public static final float GRAVITY = -30f;

    /** Box2D moves a body at most 2 units per step, i.e. 120 m/s at 60 Hz; stay under it. */
    public static final float MAX_BIRD_SPEED = 110f;

    /** Bodies leaving this box are gone (fell off the level or were blasted away). */
    public static final float KILL_LEFT = -40f;
    public static final float KILL_RIGHT = WIDTH + 60f;
    public static final float KILL_BOTTOM = -20f;

    private WorldConfig() {
    }

    public static boolean outOfBounds(float x, float y) {
        return x < KILL_LEFT || x > KILL_RIGHT || y < KILL_BOTTOM;
    }
}
