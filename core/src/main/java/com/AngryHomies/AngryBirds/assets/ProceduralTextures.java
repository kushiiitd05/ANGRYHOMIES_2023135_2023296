package com.AngryHomies.AngryBirds.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Small textures drawn in code at start-up: particle dots, clouds, stars and a white pixel.
 * Generating them keeps the repo free of extra art files and of licence questions.
 */
public final class ProceduralTextures implements Disposable {
    private final Texture circle;
    private final Texture cloud;
    private final Texture star;
    private final Texture starEmpty;
    private final Texture pixel;
    private final Texture[] cracks = new Texture[3];
    private final Texture ring;

    public ProceduralTextures() {
        circle = softCircle(64);
        cloud = cloud(256, 128);
        star = star(128, new Color(1f, 0.83f, 0.1f, 1f), new Color(0.55f, 0.32f, 0f, 1f));
        starEmpty = star(128, new Color(0.25f, 0.25f, 0.3f, 0.75f), new Color(0.1f, 0.1f, 0.12f, 0.9f));
        pixel = solid();
        ring = ring(128);
        // Each stage keeps the previous stage's lines and adds more, so damage only ever grows.
        Pixmap crackMap = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
        int[] linesPerStage = {3, 5, 8};
        for (int stage = 0; stage < cracks.length; stage++) {
            drawCracks(crackMap, linesPerStage[stage], 1000L + stage);
            cracks[stage] = new Texture(crackMap);
            cracks[stage].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        crackMap.dispose();
    }

    /** @param stage 1..3, increasingly broken */
    public Texture crack(int stage) {
        return cracks[MathUtils.clamp(stage, 1, 3) - 1];
    }

    /** A soft ring used for shockwaves. */
    public Texture ring() {
        return ring;
    }

    private static void drawCracks(Pixmap pixmap, int lines, long seed) {
        java.util.Random random = new java.util.Random(seed);
        pixmap.setColor(1f, 1f, 1f, 1f);
        int size = pixmap.getWidth();
        for (int i = 0; i < lines; i++) {
            float x = random.nextFloat() * size;
            float y = random.nextFloat() * size;
            float heading = random.nextFloat() * MathUtils.PI2;
            int segments = 6 + random.nextInt(8);
            for (int s = 0; s < segments; s++) {
                heading += (random.nextFloat() - 0.5f) * 1.3f;
                float length = 14f + random.nextFloat() * 34f;
                float nx = x + MathUtils.cos(heading) * length;
                float ny = y + MathUtils.sin(heading) * length;
                thickLine(pixmap, x, y, nx, ny, s < 2 ? 3 : 2);
                if (random.nextFloat() < 0.25f) { // short side branch
                    float bh = heading + (random.nextBoolean() ? 1f : -1f) * (0.6f + random.nextFloat());
                    thickLine(pixmap, nx, ny, nx + MathUtils.cos(bh) * length * 0.6f,
                        ny + MathUtils.sin(bh) * length * 0.6f, 1);
                }
                x = nx;
                y = ny;
            }
        }
    }

    private static void thickLine(Pixmap pixmap, float x0, float y0, float x1, float y1, int thickness) {
        for (int t = -thickness / 2; t <= thickness / 2; t++) {
            pixmap.drawLine((int) x0 + t, (int) y0, (int) x1 + t, (int) y1);
            pixmap.drawLine((int) x0, (int) y0 + t, (int) x1, (int) y1 + t);
        }
    }

    private static Texture ring(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x + 0.5f - r, y + 0.5f - r) / r;
                float a = MathUtils.clamp(1f - Math.abs(d - 0.85f) * 9f, 0f, 1f);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        return toTexture(pixmap);
    }

    public TextureRegion circle() {
        return new TextureRegion(circle);
    }

    public Texture circleTexture() {
        return circle;
    }

    public Texture cloud() {
        return cloud;
    }

    public Texture star() {
        return star;
    }

    public Texture starEmpty() {
        return starEmpty;
    }

    public Texture pixel() {
        return pixel;
    }

    private static Texture softCircle(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x + 0.5f - r, y + 0.5f - r) / r;
                float a = MathUtils.clamp((1f - d) * 3f, 0f, 1f);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        return toTexture(pixmap);
    }

    private static Texture cloud(int w, int h) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        float[][] blobs = {{0.16f, 0.34f, 0.13f}, {0.3f, 0.48f, 0.2f}, {0.47f, 0.6f, 0.26f}, {0.62f, 0.52f, 0.22f},
            {0.76f, 0.42f, 0.17f}, {0.88f, 0.33f, 0.11f}, {0.4f, 0.3f, 0.18f}, {0.58f, 0.3f, 0.18f}};
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float best = 0f;
                for (float[] b : blobs) {
                    float dx = (x / (float) w - b[0]) * 2f;
                    float dy = ((h - 1 - y) / (float) h - b[1]) * 1f; // pixmap rows run top-down
                    float d = (float) Math.sqrt(dx * dx + dy * dy) / b[2];
                    best = Math.max(best, MathUtils.clamp((1f - d) * 1.8f, 0f, 1f));
                }
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, best * 0.92f));
            }
        }
        return toTexture(pixmap);
    }

    private static Texture star(int size, Color fill, Color outline) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        fillStar(pixmap, size, size * 0.48f, outline);
        fillStar(pixmap, size, size * 0.40f, fill);
        return toTexture(pixmap);
    }

    private static void fillStar(Pixmap pixmap, int size, float outer, Color color) {
        pixmap.setColor(color);
        float c = size / 2f;
        float inner = outer * 0.48f;
        float[] xs = new float[10];
        float[] ys = new float[10];
        for (int i = 0; i < 10; i++) {
            float radius = i % 2 == 0 ? outer : inner;
            float angle = MathUtils.PI / 2f + i * MathUtils.PI / 5f;
            xs[i] = c + radius * MathUtils.cos(angle);
            ys[i] = c - radius * MathUtils.sin(angle);
        }
        for (int i = 0; i < 10; i++) {
            int j = (i + 1) % 10;
            pixmap.fillTriangle((int) c, (int) c, (int) xs[i], (int) ys[i], (int) xs[j], (int) ys[j]);
        }
    }

    private static Texture solid() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        return toTexture(pixmap);
    }

    private static Texture toTexture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        circle.dispose();
        cloud.dispose();
        star.dispose();
        starEmpty.dispose();
        pixel.dispose();
        ring.dispose();
        for (Texture crack : cracks) {
            crack.dispose();
        }
    }
}
