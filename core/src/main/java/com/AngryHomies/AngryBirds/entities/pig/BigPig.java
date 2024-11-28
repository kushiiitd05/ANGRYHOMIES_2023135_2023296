package com.AngryHomies.AngryBirds.entities.pig;

import com.AngryHomies.AngryBirds.Serializables.SerializablePig;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

public class BigPig extends Pig {

    public static final String id = "big_pig";
    public BigPig(Texture texture,String id, float x, float y, float width, float height,float density ,float diameter,float hth,World world){
        super(texture, id, x, y, width, height,density, diameter ,hth,world);
    }

    public BigPig(Texture texture, SerializablePig serializablePig, World world){
        super(texture, serializablePig, world);
    }

    public BigPig(){
    }
}

