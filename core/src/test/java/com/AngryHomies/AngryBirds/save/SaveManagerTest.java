package com.AngryHomies.AngryBirds.save;

import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveManagerTest {
    @TempDir
    File temp;

    private static SaveData sample() {
        SaveData data = new SaveData();
        data.levelId = 2;
        data.score = 12345;
        data.savedAt = 1700000000000L;
        data.birds.add("RED");
        data.birds.add("BLACK");
        SaveData.BodyData pig = new SaveData.BodyData();
        pig.kind = "KING";
        pig.x = 150.5f;
        pig.y = 25.25f;
        pig.angle = 0.3f;
        pig.health = 42f;
        pig.radius = 5.75f;
        data.pigs.add(pig);
        SaveData.BodyData block = new SaveData.BodyData();
        block.kind = "GLASS";
        block.texture = "glass_block.png";
        block.w = 4f;
        block.h = 25f;
        data.blocks.add(block);
        return data;
    }

    @Test
    void roundTripKeepsEverything() {
        SaveManager saves = new SaveManager(new FileHandle(temp));
        saves.save(sample());
        assertTrue(saves.exists(2));
        SaveData loaded = saves.load(2);
        assertNotNull(loaded);
        assertEquals(12345, loaded.score);
        assertEquals(2, loaded.birds.size);
        assertEquals("BLACK", loaded.birds.get(1));
        assertEquals("KING", loaded.pigs.first().kind);
        assertEquals(150.5f, loaded.pigs.first().x, 1e-5f);
        assertEquals(42f, loaded.pigs.first().health, 1e-5f);
        assertEquals("glass_block.png", loaded.blocks.first().texture);
    }

    @Test
    void emptyOrCorruptSlotsLoadAsNull() {
        SaveManager saves = new SaveManager(new FileHandle(temp));
        assertNull(saves.load(1));
        new FileHandle(new File(temp, "level1.json")).writeString("{ not json", false);
        assertNull(saves.load(1));
    }

    @Test
    void oldVersionsAreRejected() {
        SaveManager saves = new SaveManager(new FileHandle(temp));
        SaveData old = sample();
        old.version = 1;
        saves.save(old);
        assertNull(saves.load(2));
    }

    @Test
    void deleteEmptiesTheSlot() {
        SaveManager saves = new SaveManager(new FileHandle(temp));
        saves.save(sample());
        saves.delete(2);
        assertFalse(saves.exists(2));
    }
}
