package com.AngryHomies.AngryBirds.entities.bird;

import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BirdTest {

    private World world;
    private Texture texture;
    private MockBird bird;

    @BeforeEach
    void setUp() {
        world = new World(new Vector2(0, -9.8f), true);
        texture = mock(Texture.class);
        bird = new MockBird(texture, "bird1", 10f, 15f, 1.5f, 1.5f, 1.0f, 1.0f, 5f, world);
    }

    @Test
    void testConstructorInitialization() {
        assertEquals("bird1", bird.getId(), "Bird ID should be initialized correctly.");
        assertEquals(10f, bird.getX(), "Bird X position should be initialized correctly.");
        assertEquals(15f, bird.getY(), "Bird Y position should be initialized correctly.");
        assertEquals(1.5f, bird.getWidth(), "Bird width should be initialized correctly.");
        assertEquals(1.5f, bird.getHeight(), "Bird height should be initialized correctly.");
        assertNotNull(bird.getBody(), "Bird body should be initialized.");
        assertEquals(texture, bird.getTexture(), "Bird texture should be initialized correctly.");
    }

    @Test
    void testSetPosition() {
        bird.setPosition(20f, 25f);

        Vector2 position = bird.getBody().getPosition();
        assertEquals(20f, position.x, "Bird X position should be updated.");
        assertEquals(25f, position.y, "Bird Y position should be updated.");
    }

    @Test
    void testSetAndGetAttackPower() {
        bird.setAttackPower(10f);
        assertEquals(10f, bird.getAttackPower(), "Bird attack power should be updated correctly.");
    }

    @Test
    void testIsLaunched() {
        assertFalse(bird.isLaunched(), "Bird should not be launched initially.");
        bird.setLaunched(true);
        assertTrue(bird.isLaunched(), "Bird should be marked as launched after calling setLaunched(true).");
    }

    @Test
    void testResetLaunchTime() {
        bird.launchTime = 5f;
        bird.resetLaunchTime();
        assertEquals(0f, bird.getLaunchTime(), "Launch time should be reset to 0.");
    }

    @Test
    void testAbilityUsed() {
        assertFalse(bird.isAbilityUsed(), "Special ability should not be used initially.");
        bird.onSpecialAbility();
        assertTrue(bird.isAbilityUsed(), "Special ability should be marked as used after calling onSpecialAbility.");
    }

    class MockBird extends Bird {
        public MockBird(Texture texture, String id, float x, float y, float width, float height, float density, float diameter, float atk, World world) {
            super(texture, id, x, y, width, height, density, diameter, atk, world);
        }

        public MockBird(Texture texture, SerializableBird serializableBird, World world) {
            super(texture, serializableBird, world);
        }

        @Override
        public void onSpecialAbility() {
            this.abilityUsed = true;
        }
    }
}

