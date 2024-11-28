package com.AngryHomies.AngryBirds.entities.bird;

import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

public class RedBird extends Bird {

    public static final String id = "red_bird";

    public RedBird(Texture texture,String id, float x, float y, float width, float height,float density ,float diameter, float atk,World world) {
        super(texture,id, x, y, width, height,density,diameter,atk,world);
    }

    public RedBird(Texture texture, SerializableBird serializableBird, World world){
        super(texture, serializableBird, world);
    }

    public RedBird() {
        super();
    }
    @Override
    public void onSpecialAbility() {
        if (abilityUsed || body.getLinearVelocity().len() < 0.1f) return;
        abilityUsed = true;
        Sound Sound =Gdx.audio.newSound(Gdx.files.internal("audio/red_attack.mp3"));
        Sound.play(.5f);
        Texture specialAbilityTexture = new Texture("images/red_attack.png");
        this.textureRegion = new TextureRegion(specialAbilityTexture);
        attackPower += 5;
        System.out.println("RedBird ability triggered: Attack power increased to " + attackPower);
    }
}

