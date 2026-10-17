package com.AngryHomies.AngryBirds.level;

import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs with the assets folder as working directory (see core/build.gradle). */
class LevelDefinitionTest {
    @Test
    void everyLevelLoadsWithTheOriginalLayoutCounts() {
        int[][] expected = {{3, 3, 11}, {4, 4, 11}, {5, 7, 17}}; // birds, pigs, blocks from the old Java levels
        for (int id = 1; id <= LevelDefinition.LEVEL_COUNT; id++) {
            LevelDefinition level = LevelDefinition.load(new FileHandle(LevelDefinition.path(id)));
            assertEquals(id, level.id);
            assertEquals(expected[id - 1][0], level.birds.size, "birds in level " + id);
            assertEquals(expected[id - 1][1], level.pigs.size, "pigs in level " + id);
            assertEquals(expected[id - 1][2], level.blocks.size, "blocks in level " + id);
            for (LevelDefinition.BlockDef block : level.blocks) {
                assertNotNull(block.texture);
                assertTrue(new FileHandle("images/" + block.texture).exists(), block.texture);
            }
        }
    }

    @Test
    void stoneTexturesAreStoneMaterial() {
        LevelDefinition level = LevelDefinition.load(new FileHandle(LevelDefinition.path(3)));
        for (LevelDefinition.BlockDef block : level.blocks) {
            if (block.texture.contains("stone")) {
                assertEquals("STONE", block.material);
            }
        }
    }

    @Test
    void starThresholdsCount() {
        LevelDefinition level = LevelDefinition.load(new FileHandle(LevelDefinition.path(1)));
        assertEquals(0, level.starsFor(level.stars[0] - 1));
        assertEquals(1, level.starsFor(level.stars[0]));
        assertEquals(3, level.starsFor(level.stars[2] + 1));
    }
}
