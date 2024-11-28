package com.AngryHomies.AngryBirds.entities.block;

import com.AngryHomies.AngryBirds.Serializables.SerializableBlock;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

public class StoneBlock extends Block {

    public static final String id = "stone_block";

    public StoneBlock(Texture texture,String id, float x, float y, float width, float height,float density ,float hth , World world) {
        super(texture,id,  x, y, width, height,density,hth, world);
    }

    public StoneBlock(Texture texture, SerializableBlock serializableBlock, World world) {
        super(
            serializableBlock.getOrientation().equals("standing")
                ? new Texture(Gdx.files.internal("images/st_stoneblock.png"))
                : new Texture(Gdx.files.internal("images/stone_blocks.png")),
            serializableBlock,
            world
        );

    }
}

