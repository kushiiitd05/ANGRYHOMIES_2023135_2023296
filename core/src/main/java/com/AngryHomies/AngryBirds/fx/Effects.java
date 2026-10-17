package com.AngryHomies.AngryBirds.fx;

import com.AngryHomies.AngryBirds.assets.ProceduralTextures;
import com.AngryHomies.AngryBirds.entity.Material;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * The game's visual feedback vocabulary: debris, dust, feathers, explosions, shockwave rings,
 * dash after-images and floating score text. Gameplay code asks for an effect by meaning
 * ("a wood block broke here") and this class decides the particles.
 */
public final class Effects {
    private static final Color DUST = new Color(0.86f, 0.8f, 0.66f, 0.55f);
    private static final Color SMOKE = new Color(0.35f, 0.33f, 0.32f, 0.7f);
    private static final Color FIRE = new Color(1f, 0.55f, 0.12f, 0.9f);
    private static final Color FLASH = new Color(1f, 0.95f, 0.8f, 1f);
    private static final Color PIG_GREEN = new Color(0.45f, 0.85f, 0.2f, 0.8f);
    private static final Color TMP = new Color();

    private final ParticleSystem particles = new ParticleSystem();
    private final TextureRegion dot;
    private final ProceduralTextures textures;
    private final BitmapFont font;
    private final Array<Ring> rings = new Array<>();
    private final Array<Ghost> ghosts = new Array<>();
    private final Array<FloatingText> texts = new Array<>();
    private final Array<float[]> trail = new Array<>();

    public Effects(ProceduralTextures textures, BitmapFont font) {
        this.textures = textures;
        this.dot = textures.circle();
        this.font = font;
    }

    // ---------- effects by meaning ----------

    /** Small shards cut from the block's own texture plus a dust cloud in its colour. */
    public void debris(Material material, TextureRegion source, float x, float y, float power) {
        int count = MathUtils.clamp((int) (6 + power * 8), 6, 16);
        for (int i = 0; i < count; i++) {
            TextureRegion shard = randomCrop(source);
            float angle = MathUtils.random(15f, 165f);
            float speed = MathUtils.random(6f, 18f) * (0.6f + power);
            Particle p = particles.spawn(shard, x + MathUtils.random(-1.5f, 1.5f), y + MathUtils.random(-1f, 1f),
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.6f, 1.1f),
                MathUtils.random(0.8f, 1.9f), 0.5f, Color.WHITE);
            p.gravity = -32f;
            p.spin = MathUtils.random(-540f, 540f);
            p.rotation = MathUtils.random(360f);
        }
        dust(x, y, 6, material.debrisColor());
    }

    public void dust(float x, float y, int count, Color tint) {
        TMP.set(DUST).lerp(tint, 0.4f);
        TMP.a = DUST.a;
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(2f, 6f);
            Particle p = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed + 2f,
                MathUtils.random(0.5f, 0.9f), MathUtils.random(2f, 3.5f), MathUtils.random(5f, 8f), TMP);
            p.drag = 3f;
        }
    }

    public void feathers(Color color, float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(5f, 14f);
            Particle p = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.8f, 1.4f), 1.4f, 0.9f, color);
            p.gravity = -6f;
            p.drag = 2.5f;
            p.stretch = 2.2f;
            p.spin = MathUtils.random(-300f, 300f);
        }
    }

    public void explosion(float x, float y, float radius) {
        Particle flash = particles.spawn(dot, x, y, 0f, 0f, 0.18f, radius * 0.6f, radius * 1.6f, FLASH);
        flash.additive = true;
        for (int i = 0; i < 28; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(8f, 26f);
            TMP.set(FIRE).lerp(Color.YELLOW, MathUtils.random(0.5f));
            Particle fire = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.3f, 0.6f), MathUtils.random(3f, 6f), MathUtils.random(7f, 11f), TMP);
            fire.drag = 4f;
            fire.additive = true;
        }
        for (int i = 0; i < 16; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(3f, 9f);
            Particle smoke = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed + 4f,
                MathUtils.random(1.2f, 2.2f), MathUtils.random(5f, 8f), MathUtils.random(12f, 18f), SMOKE);
            smoke.drag = 1.5f;
            smoke.gravity = 3f;
        }
        ring(x, y, radius, Color.WHITE, 0.35f);
    }

    public void pigPop(float x, float y) {
        for (int i = 0; i < 14; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(4f, 12f);
            Particle p = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.5f, 0.9f), MathUtils.random(3f, 5f), MathUtils.random(6f, 9f), PIG_GREEN);
            p.drag = 3.5f;
        }
        ring(x, y, 9f, PIG_GREEN, 0.3f);
    }

    /** Streaks blowing backwards from a dashing bird. */
    public void speedBurst(float x, float y, float headingDeg) {
        for (int i = 0; i < 12; i++) {
            float angle = headingDeg + 180f + MathUtils.random(-25f, 25f);
            float speed = MathUtils.random(18f, 34f);
            Particle p = particles.spawn(dot, x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.2f, 0.35f), 1.2f, 0.4f, Color.WHITE);
            p.stretch = 4f;
            p.rotation = angle;
            p.additive = true;
        }
    }

    /** Particles orbiting inwards around a point: the whirlpool. */
    public void swirl(float x, float y, float radius, float phaseDeg) {
        for (int i = 0; i < 3; i++) {
            float angle = phaseDeg + i * 120f + MathUtils.random(-15f, 15f);
            float r = radius * MathUtils.random(0.6f, 1f);
            float px = x + MathUtils.cosDeg(angle) * r;
            float py = y + MathUtils.sinDeg(angle) * r;
            // tangential + inward velocity makes the spiral
            float tangent = angle + 90f;
            float vx = MathUtils.cosDeg(tangent) * 30f - (px - x) * 2.2f;
            float vy = MathUtils.sinDeg(tangent) * 30f - (py - y) * 2.2f;
            TMP.set(0.95f, 0.95f, 1f, 0.75f);
            Particle p = particles.spawn(dot, px, py, vx, vy, 0.35f, 1.6f, 0.4f, TMP);
            p.stretch = 2.5f;
            p.rotation = tangent;
            p.additive = true;
        }
    }

    /** The clean blade cut: a long bright streak along the strike line, plus sparks. */
    public void slash(float x1, float y1, float x2, float y2) {
        float angle = MathUtils.atan2(y2 - y1, x2 - x1) * MathUtils.radiansToDegrees;
        float length = (float) Math.hypot(x2 - x1, y2 - y1);
        Particle streak = particles.spawn(dot, (x1 + x2) / 2f, (y1 + y2) / 2f, 0f, 0f, 0.28f, 1.6f, 0.3f, Color.WHITE);
        streak.stretch = length / 1.6f;
        streak.rotation = angle;
        streak.additive = true;
        Particle glow = particles.spawn(dot, (x1 + x2) / 2f, (y1 + y2) / 2f, 0f, 0f, 0.2f, 4f, 1f,
            new Color(1f, 0.85f, 0.85f, 0.5f));
        glow.stretch = length / 4f;
        glow.rotation = angle;
        glow.additive = true;
        for (int i = 0; i < 18; i++) {
            float t = MathUtils.random();
            float spark = angle + (MathUtils.randomBoolean() ? 90f : -90f) + MathUtils.random(-30f, 30f);
            float speed = MathUtils.random(8f, 20f);
            Particle p = particles.spawn(dot, x1 + (x2 - x1) * t, y1 + (y2 - y1) * t,
                MathUtils.cosDeg(spark) * speed, MathUtils.sinDeg(spark) * speed, MathUtils.random(0.2f, 0.4f),
                0.8f, 0.2f, Color.WHITE);
            p.stretch = 3f;
            p.rotation = spark;
            p.additive = true;
        }
    }

    /** Small white dot left behind a flying bird (the classic "last shot" trail). */
    public void trailPuff(float x, float y, boolean big) {
        trail.add(new float[] {x, y, big ? 2.2f : 1.1f});
    }

    /** Clears only the previous shot's trail; debris and smoke keep playing. */
    public void clearTrail() {
        trail.clear();
    }

    public void ring(float x, float y, float radius, Color color, float duration) {
        rings.add(new Ring(x, y, radius, color, duration));
    }

    public void afterimage(TextureRegion region, float x, float y, float w, float h, float angle) {
        ghosts.add(new Ghost(region, x, y, w, h, angle));
    }

    public void floatingText(float x, float y, String text, Color color, boolean big) {
        texts.add(new FloatingText(x, y, text, color, big));
    }

    // ---------- frame ----------

    public void update(float delta) {
        particles.update(delta);
        for (int i = rings.size - 1; i >= 0; i--) {
            if ((rings.get(i).age += delta) >= rings.get(i).duration) {
                rings.removeIndex(i);
            }
        }
        for (int i = ghosts.size - 1; i >= 0; i--) {
            if ((ghosts.get(i).age += delta) >= Ghost.LIFE) {
                ghosts.removeIndex(i);
            }
        }
        for (int i = texts.size - 1; i >= 0; i--) {
            if ((texts.get(i).age += delta) >= FloatingText.LIFE) {
                texts.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        Color old = batch.getColor().cpy();
        batch.setColor(1f, 1f, 1f, 0.85f);
        for (float[] t : trail) {
            batch.draw(dot, t[0] - t[2] / 2f, t[1] - t[2] / 2f, t[2], t[2]);
        }
        for (Ghost g : ghosts) {
            batch.setColor(1f, 1f, 1f, 0.45f * (1f - g.age / Ghost.LIFE));
            batch.draw(g.region, g.x - g.w / 2f, g.y - g.h / 2f, g.w / 2f, g.h / 2f, g.w, g.h, 1f, 1f, g.angle);
        }
        particles.render(batch);
        for (Ring r : rings) {
            float t = r.age / r.duration;
            float size = r.radius * 2f * Interpolation.pow2Out.apply(t);
            batch.setColor(r.color.r, r.color.g, r.color.b, r.color.a * (1f - t));
            batch.draw(textures.ring(), r.x - size / 2f, r.y - size / 2f, size, size);
        }
        batch.setColor(old);
        renderTexts(batch);
    }

    private void renderTexts(SpriteBatch batch) {
        if (font == null || texts.isEmpty()) {
            return;
        }
        float oldX = font.getData().scaleX;
        float oldY = font.getData().scaleY;
        Color oldColor = font.getColor().cpy();
        for (FloatingText t : texts) {
            float progress = t.age / FloatingText.LIFE;
            float pop = progress < 0.15f ? Interpolation.swingOut.apply(progress / 0.15f) : 1f;
            float scale = (t.big ? 0.11f : 0.075f) * pop;
            font.getData().setScale(scale * oldX / 1.5f);
            font.setColor(t.color.r, t.color.g, t.color.b, progress < 0.7f ? 1f : 1f - (progress - 0.7f) / 0.3f);
            font.draw(batch, t.text, t.x - 10f, t.y + progress * 6f, 20f, com.badlogic.gdx.utils.Align.center, false);
        }
        font.getData().setScale(oldX, oldY);
        font.setColor(oldColor);
    }

    private static TextureRegion randomCrop(TextureRegion source) {
        int w = Math.max(4, source.getRegionWidth() / 4);
        int h = Math.max(4, source.getRegionHeight() / 4);
        int x = source.getRegionX() + MathUtils.random(Math.max(0, source.getRegionWidth() - w));
        int y = source.getRegionY() + MathUtils.random(Math.max(0, source.getRegionHeight() - h));
        return new TextureRegion(source.getTexture(), x, y, w, h);
    }

    private static final class Ring {
        final float x;
        final float y;
        final float radius;
        final Color color;
        final float duration;
        float age;

        Ring(float x, float y, float radius, Color color, float duration) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = new Color(color);
            this.duration = duration;
        }
    }

    private static final class Ghost {
        static final float LIFE = 0.25f;
        final TextureRegion region;
        final float x;
        final float y;
        final float w;
        final float h;
        final float angle;
        float age;

        Ghost(TextureRegion region, float x, float y, float w, float h, float angle) {
            this.region = region;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.angle = angle;
        }
    }

    private static final class FloatingText {
        static final float LIFE = 1.3f;
        final float x;
        final float y;
        final String text;
        final Color color;
        final boolean big;
        float age;

        FloatingText(float x, float y, String text, Color color, boolean big) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = new Color(color);
            this.big = big;
        }
    }
}
