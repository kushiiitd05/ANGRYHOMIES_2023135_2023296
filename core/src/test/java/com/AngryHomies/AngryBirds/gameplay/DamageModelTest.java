package com.AngryHomies.AngryBirds.gameplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageModelTest {
    @Test
    void gentleContactsDoNoDamage() {
        assertEquals(0f, DamageModel.impactEnergy(30f, 30f, DamageModel.MIN_IMPACT_SPEED - 0.1f));
    }

    @Test
    void staticSideUsesTheFullDynamicMass() {
        assertEquals(10f, DamageModel.effectiveMass(10f, 0f), 1e-6f);
        assertEquals(5f, DamageModel.effectiveMass(10f, 10f), 1e-6f);
    }

    @Test
    void energyGrowsWithTheSquareOfSpeed() {
        float slow = DamageModel.impactEnergy(20f, 0f, 10f);
        float fast = DamageModel.impactEnergy(20f, 0f, 20f);
        assertEquals(4f, fast / slow, 1e-4f);
    }

    @Test
    void tougherMaterialsTakeLessDamage() {
        float energy = 40000f;
        assertTrue(DamageModel.damage(energy, 0.55f, 1f) > DamageModel.damage(energy, 2.2f, 1f));
        assertEquals(0f, DamageModel.damage(energy, Float.MAX_VALUE, 1f));
    }

    @Test
    void fullSpeedRedBirdBreaksASmallWoodBlock() {
        // red bird ~87 kg vs 4x4 m wood block (32 kg) at 60 m/s; small wood block health is 55
        float energy = DamageModel.impactEnergy(87f, 32f, 60f);
        assertTrue(DamageModel.damage(energy, 1f, 1f) > 55f);
    }

    @Test
    void blastFalloffIsFullAtCentreAndZeroAtEdge() {
        assertEquals(1f, DamageModel.falloff(0f, 10f), 1e-6f);
        assertEquals(0.25f, DamageModel.falloff(5f, 10f), 1e-6f);
        assertEquals(0f, DamageModel.falloff(10f, 10f), 1e-6f);
    }
}
