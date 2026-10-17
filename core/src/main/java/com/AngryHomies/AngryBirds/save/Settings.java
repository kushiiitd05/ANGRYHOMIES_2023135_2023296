package com.AngryHomies.AngryBirds.save;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/** Player options, persisted in libGDX {@link Preferences} so they survive restarts. */
public final class Settings {
    private static final String MASTER = "volume.master";
    private static final String MUSIC = "volume.music";
    private static final String SFX = "volume.sfx";
    private static final String TRAJECTORY = "aim.trajectory";
    private static final String FULLSCREEN = "display.fullscreen";

    private final Preferences prefs;

    public Settings(Preferences prefs) {
        this.prefs = prefs;
    }

    public float masterVolume() {
        return prefs.getFloat(MASTER, 1f);
    }

    public float musicVolume() {
        return prefs.getFloat(MUSIC, 0.6f);
    }

    public float sfxVolume() {
        return prefs.getFloat(SFX, 0.9f);
    }

    public boolean showTrajectory() {
        return prefs.getBoolean(TRAJECTORY, true);
    }

    public boolean isFullscreen() {
        return prefs.getBoolean(FULLSCREEN, false);
    }

    public void setMasterVolume(float value) {
        putFloat(MASTER, value);
    }

    public void setMusicVolume(float value) {
        putFloat(MUSIC, value);
    }

    public void setSfxVolume(float value) {
        putFloat(SFX, value);
    }

    public void setShowTrajectory(boolean value) {
        prefs.putBoolean(TRAJECTORY, value).flush();
    }

    public void setFullscreen(boolean value) {
        prefs.putBoolean(FULLSCREEN, value).flush();
    }

    private void putFloat(String key, float value) {
        prefs.putFloat(key, MathUtils.clamp(value, 0f, 1f)).flush();
    }
}
