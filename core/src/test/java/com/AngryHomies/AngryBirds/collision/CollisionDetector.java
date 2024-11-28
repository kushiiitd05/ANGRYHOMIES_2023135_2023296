package com.AngryHomies.AngryBirds.collision;

import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.AngryHomies.AngryBirds.entities.block.Block;
import com.AngryHomies.AngryBirds.entities.pig.Pig;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollisionDetectorTest {

    private World world;
    private CollisionDetector collisionDetector;
    private List<Block> blocks;
    private List<Pig> pigs;

    @BeforeEach
    void setUp() {
        world = new World(new Vector2(0, -8f), true);
        collisionDetector = new CollisionDetector();
        blocks = new ArrayList<>();
        pigs = new ArrayList<>();
        world.setContactListener(collisionDetector);
    }

    @Test
    void testBirdBlockCollision() {
        Bird bird = mock(Bird.class);
        Block block = mock(Block.class);

        when(bird.getAttackPower()).thenReturn(6f);
        when(block.getHealth()).thenReturn(10f).thenReturn(4f);

        createMockBodyWithUserData(world, bird);
        createMockBodyWithUserData(world, block);

        simulateCollision(bird, block);

        verify(block).takeDamage(6);
        assertEquals(0, CollisionDetector.blocksToRemove.size());
    }

    @Test
    void testBirdPigCollision() {
        Bird bird = mock(Bird.class);
        Pig pig = mock(Pig.class);

        when(bird.getAttackPower()).thenReturn(6f);
        when(pig.getHealth()).thenReturn(10f).thenReturn(4f);

        createMockBodyWithUserData(world, bird);
        createMockBodyWithUserData(world, pig);

        simulateCollision(bird, pig);

        verify(pig).takeDamage(6);
        assertEquals(0, CollisionDetector.pigsToRemove.size());
    }

    private void createMockBodyWithUserData(World world, Object userData) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bodyDef);
        body.setUserData(userData);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = new CircleShape();
        fixtureDef.shape.setRadius(1.0f);
        body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();
    }

    private void simulateCollision(Object objA, Object objB) {
        Contact contact = mock(Contact.class);
        Fixture fixtureA = mock(Fixture.class);
        Fixture fixtureB = mock(Fixture.class);
        when(contact.getFixtureA()).thenReturn(fixtureA);
        when(contact.getFixtureB()).thenReturn(fixtureB);
        when(fixtureA.getBody()).thenReturn(mock(Body.class));
        when(fixtureB.getBody()).thenReturn(mock(Body.class));
        when(fixtureA.getBody().getUserData()).thenReturn(objA);
        when(fixtureB.getBody().getUserData()).thenReturn(objB);

        collisionDetector.beginContact(contact);
    }
}

