package com.AngryHomies.AngryBirds.Serializables;

import com.AngryHomies.AngryBirds.entities.bird.Bird;
import com.AngryHomies.AngryBirds.entities.block.Block;
import com.AngryHomies.AngryBirds.entities.pig.Pig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelState implements Serializable {

    private List<SerializableBird> birds;
    private List<SerializablePig> pigs;
    private List<SerializableBlock> blocks;
    private SerializableBird currentBird;
    private boolean isPaused;

    public LevelState(List<Bird> birds, List<Pig> pigs, List<Block> blocks, Bird currentBird) {
        this.birds = new ArrayList<>();
        for (Bird bird : birds) {
            this.birds.add(new SerializableBird(bird));
        }

        this.pigs = new ArrayList<>();
        for (Pig pig : pigs) {
            this.pigs.add(new SerializablePig(pig));
        }

        this.blocks = new ArrayList<>();
        for (Block block : blocks) {
            this.blocks.add(new SerializableBlock(block));
        }

        this.currentBird = currentBird != null ? new SerializableBird(currentBird) : null;
        this.isPaused = true;
    }

    public List<SerializableBird> getBirds() {
        return birds;
    }

    public void setBirds(List<SerializableBird> birds) {
        this.birds = birds;
    }

    public List<SerializablePig> getPigs() {
        return pigs;
    }

    public void setPigs(List<SerializablePig> pigs) {
        this.pigs = pigs;
    }

    public List<SerializableBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<SerializableBlock> blocks) {
        this.blocks = blocks;
    }

    public SerializableBird getCurrentBird() {
        return currentBird;
    }

    public void setCurrentBird(SerializableBird currentBird) {
        this.currentBird = currentBird;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
