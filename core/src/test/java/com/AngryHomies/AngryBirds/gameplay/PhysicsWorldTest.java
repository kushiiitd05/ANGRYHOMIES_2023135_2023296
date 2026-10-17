package com.AngryHomies.AngryBirds.gameplay;

import com.badlogic.gdx.physics.box2d.Box2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsWorldTest {
    private static final PhysicsWorld.StepListener NONE = new PhysicsWorld.StepListener() {
        @Override
        public void beforeStep() {
        }

        @Override
        public void afterStep() {
        }
    };

    @BeforeAll
    static void loadNatives() {
        Box2D.init();
    }

    private static int stepsOver(float seconds, int frameRate) {
        PhysicsWorld physics = new PhysicsWorld();
        int steps = 0;
        for (int frame = 0; frame < Math.round(seconds * frameRate); frame++) {
            steps += physics.update(1f / frameRate, NONE);
            assertTrue(physics.alpha() >= 0f && physics.alpha() < 1f);
        }
        physics.dispose();
        return steps;
    }

    @Test
    void sameSimulationTimeAtAnyRefreshRate() {
        int at60 = stepsOver(2f, 60);
        int at144 = stepsOver(2f, 144);
        int at30 = stepsOver(2f, 30);
        assertEquals(120, at60, 1);
        assertEquals(at60, at144, 1);
        assertEquals(at60, at30, 1);
    }

    @Test
    void zeroDeltaFreezesTime() {
        PhysicsWorld physics = new PhysicsWorld();
        assertEquals(0, physics.update(0f, NONE));
        physics.dispose();
    }
}
