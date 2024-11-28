package com.AngryHomies.AngryBirds.logic;

import com.AngryHomies.AngryBirds.Texture.TextureFactory;
import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlingshotTest {

    private Slingshot slingshot;
    private TextureFactory textureFactory;
    private Texture slingshotTexture;
    private Texture dotTexture;
    private Bird bird;
    private World world;

    @BeforeEach
    void setUp() {
        textureFactory = mock(TextureFactory.class);
        slingshotTexture = mock(Texture.class);
        dotTexture = mock(Texture.class);
        bird = mock(Bird.class);
        world = new World(new Vector2(0, -9.8f), true);

        when(textureFactory.createSlingShotTexture()).thenReturn(dotTexture);

        slingshot = new Slingshot(slingshotTexture, 260, 780, 50, 100, textureFactory);

        Body birdBody = mock(Body.class);
        when(bird.getBody()).thenReturn(birdBody);
        when(bird.isLaunched()).thenReturn(false);
        when(birdBody.getPosition()).thenReturn(new Vector2(26, 10));
        when(birdBody.getMass()).thenReturn(1.0f);
    }

    @Test
    void testCalculateTrajectory() {
        Vector2 initialPosition = new Vector2(26, 10);
        Vector2 initialVelocity = new Vector2(-5, 10);
        float gravity = -8f;
        float timeStep = 0.1f;
        int maxSteps = 10;

        slingshot.calculateTrajectory(initialPosition, initialVelocity, gravity, timeStep, maxSteps);

        List<Vector2> trajectoryDots = slingshot.getTrajectoryDots();
        assertNotNull(trajectoryDots, "Trajectory dots should not be null after calculation.");
        assertEquals(maxSteps, trajectoryDots.size(), "Number of trajectory dots should match maxSteps.");
        assertTrue(trajectoryDots.get(0).x > 0 && trajectoryDots.get(0).y > 0, "Trajectory dots should be valid positions.");
    }

    @Test
    void testRenderTrajectory() {
        SpriteBatch batch = mock(SpriteBatch.class);

        slingshot.calculateTrajectory(new Vector2(26, 10), new Vector2(-5, 10), -9.8f, 0.1f, 10);
        slingshot.renderTrajectory(batch);

        ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
        verify(batch, atLeastOnce()).draw(eq(dotTexture), xCaptor.capture(), yCaptor.capture(), eq(8f), eq(8f));
        assertFalse(xCaptor.getAllValues().isEmpty(), "X positions of trajectory dots should be captured.");
        assertFalse(yCaptor.getAllValues().isEmpty(), "Y positions of trajectory dots should be captured.");
    }

    @Test
    void testGetMaxLaunchPower() {
        assertEquals(4500f, Slingshot.getMaxLaunchPower(), "Max launch power should match the constant value.");
    }
}

