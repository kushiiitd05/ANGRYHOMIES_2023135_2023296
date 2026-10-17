package com.AngryHomies.AngryBirds.save;

import com.badlogic.gdx.utils.Array;

/**
 * Plain data for one saved level, written as JSON. Public fields and no-arg constructors are
 * what libGDX's {@code Json} reflection needs; the version lets future builds reject old files.
 */
public final class SaveData {
    public static final int CURRENT_VERSION = 2;

    public int version = CURRENT_VERSION;
    public int levelId;
    public long savedAt;
    public int score;
    /** Birds not yet launched, in launch order (the one on the slingshot first). */
    public Array<String> birds = new Array<>();
    public Array<BodyData> pigs = new Array<>();
    public Array<BodyData> blocks = new Array<>();

    /** Transform, motion and health of one pig or block. */
    public static final class BodyData {
        /** Pig type or block material name. */
        public String kind;
        /** Block texture file; unused for pigs. */
        public String texture;
        public float x;
        public float y;
        public float angle;
        public float vx;
        public float vy;
        public float spin;
        public float w;
        public float h;
        public float density;
        public float radius;
        public float health;
    }
}
