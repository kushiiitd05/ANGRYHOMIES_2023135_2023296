package com.AngryHomies.AngryBirds.collision;

import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.AngryHomies.AngryBirds.entities.block.Block;
import com.AngryHomies.AngryBirds.entities.pig.Pig;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;

public class CollisionDetector implements ContactListener {
    static List<String> blocksToRemove = new ArrayList<>();
    static List<String> pigsToRemove = new ArrayList<>();

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object objA = fixtureA.getBody().getUserData();
        Object objB = fixtureB.getBody().getUserData();

        if ((objA instanceof Bird && objB instanceof Block) || (objB instanceof Bird && objA instanceof Block)) {
            Bird bird = objA instanceof Bird ? (Bird) objA : (Bird) objB;
            Block block = objA instanceof Block ? (Block) objA : (Block) objB;
            handleBirdBlockCollision(bird, block);
        }

        if (objA instanceof Pig && objB instanceof Pig) {
            Pig pigA = (Pig) objA;
            Pig pigB = (Pig) objB;
            handlePigPigCollision(pigA, pigB);
        }

        if ((objA instanceof Bird && objB instanceof Pig) || (objB instanceof Bird && objA instanceof Pig)) {
            Bird bird = objA instanceof Bird ? (Bird) objA : (Bird) objB;
            Pig pig = objA instanceof Pig ? (Pig) objA : (Pig) objB;
            handleBirdPigCollision(bird, pig);
        }

        if ((objA instanceof Pig && objB == "Ground") || (objB instanceof Pig && objA == "Ground")) {
            Pig pig = objA instanceof Pig ? (Pig) objA : (Pig) objB;
            handlePigGroundCollision(pig);
        }

        if ((objA instanceof Block && objB == "Ground") || (objB instanceof Block && objA == "Ground")) {
            Block block = objA instanceof Block ? (Block) objA : (Block) objB;
            handleBlockGroundCollision(block);
        }
    }

    private void handleBirdBlockCollision(Bird bird, Block block) {
        block.takeDamage(bird.getAttackPower());
        if (block.getHealth() <= 0) {
            blocksToRemove.add(block.getId());
        }
    }

    private void handleBirdPigCollision(Bird bird, Pig pig) {
        pig.takeDamage(bird.getAttackPower());
        if (pig.getHealth() <= 0) {
            pigsToRemove.add(pig.getId());
        }
    }

    private void handlePigGroundCollision(Pig pig) {
        pig.takeDamage(4);
        if (pig.getHealth() <= 0) {
            pigsToRemove.add(pig.getId());
        }
    }

    private void handleBlockGroundCollision(Block block) {
        if (!block.isOnGround()) {
            block.setOnGround(true);
            block.takeDamage(2);
            if (block.getHealth() <= 0) {
                blocksToRemove.add(block.getId());
            }
        }
    }

    private void handlePigPigCollision(Pig pigA, Pig pigB) {
        pigA.takeDamage(5);
        pigB.takeDamage(5);

        if (pigA.getHealth() <= 0) {
            pigsToRemove.add(pigA.getId());
        }
        if (pigB.getHealth() <= 0) {
            pigsToRemove.add(pigB.getId());
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    public static void processRemovals(World world, List<Block> blocks, List<Pig> pigs) {
        for (String blockId : blocksToRemove) {
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).getId() == blockId) {
                    world.destroyBody(blocks.get(i).getBody());
                    blocks.remove(i);
                    break;
                }
            }
        }
        blocksToRemove.clear();

        for (String pigId : pigsToRemove) {
            for (int i = 0; i < pigs.size(); i++) {
                if (pigs.get(i).getId() == pigId) {
                    world.destroyBody(pigs.get(i).getBody());
                    pigs.remove(i);
                    break;
                }
            }
        }
        pigsToRemove.clear();
    }
}

