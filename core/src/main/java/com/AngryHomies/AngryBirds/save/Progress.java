package com.AngryHomies.AngryBirds.save;

import com.badlogic.gdx.Preferences;

/** Which levels are unlocked, plus best score and stars per level. */
public final class Progress {
    private final Preferences prefs;
    private final Settings settings;

    public Progress(Preferences prefs) {
        this.prefs = prefs;
        this.settings = new Settings(prefs);
    }

    public Settings settings() {
        return settings;
    }

    public boolean isUnlocked(int level) {
        return level <= 1 || prefs.getBoolean(key(level, "unlocked"), false);
    }

    public int bestScore(int level) {
        return prefs.getInteger(key(level, "best"), 0);
    }

    public int stars(int level) {
        return prefs.getInteger(key(level, "stars"), 0);
    }

    /** Records a finished level; only improvements are kept. Unlocks the next level on a win. */
    public void recordWin(int level, int score, int stars) {
        if (score > bestScore(level)) {
            prefs.putInteger(key(level, "best"), score);
        }
        if (stars > stars(level)) {
            prefs.putInteger(key(level, "stars"), stars);
        }
        prefs.putBoolean(key(level + 1, "unlocked"), true);
        prefs.flush();
    }

    public void reset() {
        float master = settings.masterVolume();
        float music = settings.musicVolume();
        float sfx = settings.sfxVolume();
        boolean trajectory = settings.showTrajectory();
        boolean fullscreen = settings.isFullscreen();
        prefs.clear();
        settings.setMasterVolume(master);
        settings.setMusicVolume(music);
        settings.setSfxVolume(sfx);
        settings.setShowTrajectory(trajectory);
        settings.setFullscreen(fullscreen);
    }

    private static String key(int level, String field) {
        return "level" + level + "." + field;
    }
}
