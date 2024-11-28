package com.AngryHomies.AngryBirds.entities.bird;

import com.AngryHomies.AngryBirds.Serializables.SerializableBird;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;


public class BlackBird extends Bird {

    public static final String id = "black_bird";

    public BlackBird(Texture texture,String id, float x, float y, float width, float height,float density,float diameter ,float atk, World world) {
        super(texture,id,  x, y, width, height,density,diameter,atk,world);
    }

    public BlackBird(Texture texture, SerializableBird serializableBird, World world){
        super(texture, serializableBird, world);
    }
    @Override
    public void onSpecialAbility() {
        if (abilityUsed || body.getLinearVelocity().len() < 0.1f) return;
        abilityUsed = true;

        Sound Sound = Gdx.audio.newSound(Gdx.files.internal("audio/red_attack.mp3"));
        Sound.play(.5f);

        Texture specialAbilityTexture = new Texture("images/Black_bomb.png");
        this.textureRegion = new TextureRegion(specialAbilityTexture);



        World world = this.getBody().getWorld();
        Vector2 position = this.getBody().getPosition();
        float explosionRadius = 500f / 10f;
        attackPower += 2;

        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);

        for (Body body : bodies) {
            if (body == this.getBody() || body.getType() != BodyDef.BodyType.DynamicBody) continue;

            Vector2 bodyPosition = body.getPosition();
            float distance = bodyPosition.dst(position);


            if (distance <= explosionRadius) {
                Vector2 force = bodyPosition.sub(position).nor().scl(1 / distance * 500f * body.getMass());
                body.applyLinearImpulse(force, body.getWorldCenter(), true);

                System.out.println("Applied force: " + force + " to body at: " + bodyPosition + ", Distance: " + distance);
            }
        }

        System.out.println("BlackBird triggered explosion at position: " + position);
    }

}

