package com.AngryHomies.AngryBirds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.util.zip.Deflater;

/**
 * Developer-only autoplay and frame capture, active only when the {@code ANGRYHOMIES_DEMO}
 * environment variable names an output folder. Menus click through on their own, levels fire a
 * shots aimed at the pigs and trigger each special move near contact, and a game-window screenshot is saved every
 * {@link #SHOT_INTERVAL} seconds. Used to verify visuals without a human at the controls.
 */
public final class Demo {
    private static final String OUTPUT = System.getenv("ANGRYHOMIES_DEMO");
    private static final float SHOT_INTERVAL = 0.7f;
    private static final int MAX_SHOTS = 90;
    private static final float QUIT_AFTER = 75f;

    private static float clock;
    private static float nextShot;
    private static int shots;
    private static boolean saveTested;

    private Demo() {
    }

    public static boolean enabled() {
        return OUTPUT != null;
    }

    /** True exactly once per run: time to exercise save -> reload. */
    public static boolean takeSaveTest() {
        if (saveTested) {
            return false;
        }
        saveTested = true;
        return true;
    }

    /** Call after each rendered frame. */
    public static void afterFrame(float delta) {
        if (!enabled()) {
            return;
        }
        clock += delta;
        if (clock >= nextShot && shots < MAX_SHOTS) {
            nextShot += SHOT_INTERVAL;
            int w = Gdx.graphics.getBackBufferWidth();
            int h = Gdx.graphics.getBackBufferHeight();
            Pixmap frame = Pixmap.createFromFrameBuffer(0, 0, w, h);
            PixmapIO.writePNG(Gdx.files.absolute(OUTPUT + String.format("/shot_%03d.png", shots++)), frame,
                Deflater.BEST_SPEED, true);
            frame.dispose();
        }
        if (clock > QUIT_AFTER) {
            Gdx.app.exit();
        }
    }
}
