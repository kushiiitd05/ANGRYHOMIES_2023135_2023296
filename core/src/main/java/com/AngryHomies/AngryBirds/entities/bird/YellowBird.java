package com.AngryHomies.AngryBirds.entities.bird;

import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class YellowBird extends Bird {

    public static final String id = "yellow_bird";

    public YellowBird(Texture texture,String id, float x, float y, float width, float height,float density,float diameter,float atk, World world) {
        super(texture,id,  x, y, width, height,density,diameter,atk,world);
    }

    public YellowBird(Texture texture, SerializableBird serializableBird, World world){
        super(texture, serializableBird, world);
    }
    @Override
    public void onSpecialAbility() {
        if (abilityUsed || body.getLinearVelocity().len() < 0.1f) return;
        abilityUsed = true;
        Sound Sound = Gdx.audio.newSound(Gdx.files.internal("audio/red_attack.mp3"));
        Sound.play(.5f);

        Texture specialAbilityTexture = new Texture("images/yellow_speed.png");
        this.textureRegion = new TextureRegion(specialAbilityTexture);
        this.width *= 1.5f;
        this.height *= 1.5f;

        Vector2 boost = body.getLinearVelocity().nor().scl(1500f);
        body.applyLinearImpulse(boost, body.getWorldCenter(), true);
        attackPower += 4;
        System.out.println("YellowBird ability triggered: Velocity boosted, Attack power increased to " + attackPower);
    }
}

