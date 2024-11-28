package com.AngryHomies.AngryBirds.entities.block;

import com.AngryHomies.AngryBirds.Serializables.SerializableBlock;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

public class GlassBlock extends Block {
    public static final String id = "glass_block";

    public GlassBlock(Texture texture,String id, float x, float y, float width, float height,float density, float hth,World world) {
        super(texture,id,  x, y, width, height,density,hth,world);
    }

    public GlassBlock(Texture texture, SerializableBlock serializableBlock, World world) {
        super(
            serializableBlock.getOrientation().equals("standing")
                ? new Texture(Gdx.files.internal("images/glass_block.png"))
                : new Texture(Gdx.files.internal("images/sp_glassblock.png")),
            serializableBlock,
            world
        );
    }
}

