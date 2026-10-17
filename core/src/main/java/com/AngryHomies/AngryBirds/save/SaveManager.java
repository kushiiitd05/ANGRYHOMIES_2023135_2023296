package com.AngryHomies.AngryBirds.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;

/**
 * Reads and writes {@link SaveData} as JSON, one file per level slot, in a folder next to the game
 * (no hard-coded user paths, so it works on any OS).
 */
public final class SaveManager {
    private final FileHandle directory;
    private final Json json;

    public SaveManager(FileHandle directory) {
        this.directory = directory;
        this.json = new Json(JsonWriter.OutputType.json);
        json.setIgnoreUnknownFields(true);
        json.setElementType(SaveData.class, "birds", String.class);
        json.setElementType(SaveData.class, "pigs", SaveData.BodyData.class);
        json.setElementType(SaveData.class, "blocks", SaveData.BodyData.class);
    }

    public void save(SaveData data) {
        directory.mkdirs();
        file(data.levelId).writeString(json.prettyPrint(data), false);
    }

    public boolean exists(int levelId) {
        return file(levelId).exists();
    }

    /** @return the save, or {@code null} if the slot is empty, unreadable, or from an old version */
    public SaveData load(int levelId) {
        FileHandle file = file(levelId);
        if (!file.exists()) {
            return null;
        }
        try {
            SaveData data = json.fromJson(SaveData.class, file);
            return data != null && data.version == SaveData.CURRENT_VERSION ? data : null;
        } catch (SerializationException e) {
            if (Gdx.app != null) {
                Gdx.app.error("SaveManager", "Unreadable save " + file.path(), e);
            }
            return null;
        }
    }

    public void delete(int levelId) {
        file(levelId).delete();
    }

    private FileHandle file(int levelId) {
        return directory.child("level" + levelId + ".json");
    }
}
