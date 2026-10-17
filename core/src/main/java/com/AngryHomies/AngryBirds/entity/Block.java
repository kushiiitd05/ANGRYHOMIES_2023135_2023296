package com.AngryHomies.AngryBirds.entity;

import com.AngryHomies.AngryBirds.assets.ProceduralTextures;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

/**
 * A wood, glass or stone piece of a structure. Shows three crack stages as it weakens; when it
 * breaks, {@link com.AngryHomies.AngryBirds.fx.FragmentSystem} splits it into tumbling pieces.
 */
public final class Block extends GameEntity {
    private static final Color GLASS_CRACK = new Color(1f, 1f, 1f, 0.85f);
    private static final Color DARK_CRACK = new Color(0.12f, 0.08f, 0.05f, 0.8f);

    private final Material material;
    private final String textureName;
    private final float density;
    private final ProceduralTextures fx;
    private final TextureRegion crackRegion = new TextureRegion();

    public Block(World world, Material material, Texture texture, String textureName, float x, float y,
                 float angle, float width, float height, float density, ProceduralTextures fx) {
        super(BodyFactory.box(world, x, y, angle, width, height, density, material.friction(), material.restitution()),
            new TextureRegion(texture), width, height, material.healthFor(width, height));
        this.material = material;
        this.textureName = textureName;
        this.density = density;
        this.fx = fx;
    }

    /** Restores a saved block with its remaining health. */
    public Block withHealth(float remaining) {
        health = Math.min(remaining, maxHealth);
        return this;
    }

    @Override
    public void render(SpriteBatch batch, float alpha) {
        super.render(batch, alpha);
        int stage = crackStage();
        if (stage == 0 || fx == null) {
            return;
        }
        // Take a crack region with the block's aspect ratio so lines aren't stretched on long planks.
        Texture crack = fx.crack(stage);
        float aspect = height / width;
        int regionW = aspect <= 1f ? crack.getWidth() : (int) (crack.getHeight() / aspect);
        int regionH = aspect <= 1f ? (int) (crack.getWidth() * aspect) : crack.getHeight();
        crackRegion.setTexture(crack);
        crackRegion.setRegion(0, 0, Math.max(8, regionW), Math.max(8, regionH));
        Color old = batch.getColor().cpy();
        batch.setColor(material == Material.GLASS ? GLASS_CRACK : DARK_CRACK);
        float x = renderX(alpha);
        float y = renderY(alpha);
        batch.draw(crackRegion, x - width / 2f, y - height / 2f, width / 2f, height / 2f, width, height,
            1f, 1f, renderAngle(alpha));
        batch.setColor(old);
    }

    /** Exact distance to the rotated rectangle: transform into the block's frame and clamp. */
    @Override
    public float surfaceDistance(com.badlogic.gdx.math.Vector2 point) {
        float angle = body.getAngle();
        float dx = point.x - body.getPosition().x;
        float dy = point.y - body.getPosition().y;
        float cos = com.badlogic.gdx.math.MathUtils.cos(-angle);
        float sin = com.badlogic.gdx.math.MathUtils.sin(-angle);
        float lx = Math.abs(dx * cos - dy * sin) - width / 2f;
        float ly = Math.abs(dx * sin + dy * cos) - height / 2f;
        return (float) Math.hypot(Math.max(lx, 0f), Math.max(ly, 0f));
    }

    /** 0 = intact, 3 = about to break. */
    public int crackStage() {
        float ratio = healthRatio();
        if (ratio > 0.75f) {
            return 0;
        }
        return ratio > 0.5f ? 1 : ratio > 0.25f ? 2 : 3;
    }

    @Override
    public float toughness() {
        return material.toughness();
    }

    public Material material() {
        return material;
    }

    public String textureName() {
        return textureName;
    }

    public float density() {
        return density;
    }
}
