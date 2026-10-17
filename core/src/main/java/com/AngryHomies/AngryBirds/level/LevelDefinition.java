package com.AngryHomies.AngryBirds.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * A level as data, read from {@code assets/levels/levelN.json}. Coordinates are world metres
 * (the old 1920x1080 pixel layout divided by 10), so the original layouts are preserved exactly.
 */
public final class LevelDefinition {
    public static final int LEVEL_COUNT = 3;

    public int id;
    public String name;
    /** Score needed for one, two and three stars. */
    public int[] stars = new int[3];
    public Array<String> birds = new Array<>();
    public Array<PigDef> pigs = new Array<>();
    public Array<BlockDef> blocks = new Array<>();

    public static final class PigDef {
        public String type;
        public float x;
        public float y;
        public float w;
        public float h;
        public float density;
        public float radius;
    }

    public static final class BlockDef {
        public String material;
        public String texture;
        public float x;
        public float y;
        public float w;
        public float h;
        public float density;
        public float angle;
    }

    public static LevelDefinition load(FileHandle file) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setElementType(LevelDefinition.class, "birds", String.class);
        json.setElementType(LevelDefinition.class, "pigs", PigDef.class);
        json.setElementType(LevelDefinition.class, "blocks", BlockDef.class);
        LevelDefinition level = json.fromJson(LevelDefinition.class, file);
        level.validate(file.path());
        return level;
    }

    public static String path(int id) {
        return "levels/level" + id + ".json";
    }

    public int starsFor(int score) {
        int earned = 0;
        for (int threshold : stars) {
            if (score >= threshold) {
                earned++;
            }
        }
        return earned;
    }

    private void validate(String source) {
        if (birds.isEmpty() || pigs.isEmpty() || stars.length != 3) {
            throw new IllegalStateException(source + ": a level needs birds, pigs and three star thresholds");
        }
    }
}
