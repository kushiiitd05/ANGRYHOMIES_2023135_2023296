package com.AngryHomies.AngryBirds.gameplay;

import com.AngryHomies.AngryBirds.entity.Material;
import com.AngryHomies.AngryBirds.fx.ShatterPattern;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShatterPatternTest {
    private static float total(Array<float[]> pieces) {
        float sum = 0f;
        for (float[] piece : pieces) {
            sum += ShatterPattern.area(piece);
        }
        return sum;
    }

    @Test
    void piecesAlwaysTileTheWholeBlock() {
        Random random = new Random(7);
        float[][] sizes = {{4f, 4f}, {31.3f, 2.5f}, {4f, 25f}, {50f, 4f}};
        for (Material material : Material.values()) {
            for (float[] size : sizes) {
                for (int trial = 0; trial < 50; trial++) {
                    float hitX = (random.nextFloat() - 0.5f) * size[0] * 2f;
                    float hitY = (random.nextFloat() - 0.5f) * size[1] * 2f;
                    Array<float[]> pieces = ShatterPattern.pieces(material, size[0], size[1], hitX, hitY, random);
                    // thin slivers may be dropped as dust, so allow a small shortfall but never overlap
                    float area = total(pieces);
                    assertTrue(area <= size[0] * size[1] * 1.001f, material + " overlaps");
                    assertTrue(area >= size[0] * size[1] * 0.85f, material + " leaves holes");
                }
            }
        }
    }

    @Test
    void glassBreaksIntoMorePiecesThanStone() {
        Random random = new Random(3);
        int glass = 0;
        int stone = 0;
        for (int i = 0; i < 100; i++) {
            glass += ShatterPattern.pieces(Material.GLASS, 6f, 6f, 0f, 0f, random).size;
            stone += ShatterPattern.pieces(Material.STONE, 6f, 6f, 0f, 0f, random).size;
        }
        assertTrue(glass > stone * 1.8f, "glass " + glass + " vs stone " + stone);
    }

    @Test
    void woodPlanksSplinterAlongTheGrain() {
        Array<float[]> pieces = ShatterPattern.pieces(Material.WOOD, 31.3f, 2.5f, 0f, 0f, new Random(1));
        for (float[] piece : pieces) {
            assertEquals(8, piece.length, "planks split into strips (quads)");
        }
    }
}
