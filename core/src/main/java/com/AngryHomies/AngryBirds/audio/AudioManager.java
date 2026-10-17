package com.AngryHomies.AngryBirds.audio;

import com.AngryHomies.AngryBirds.save.Settings;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectFloatMap;

/**
 * Plays sound effects and music with the user's volume settings applied.
 *
 * <p>Effects get a small random pitch so repeats don't sound robotic, and the same effect can't
 * retrigger within a few milliseconds (a collapsing tower would otherwise stack 30 copies and clip).
 * Music crossfades when a screen asks for a different track.
 */
public final class AudioManager implements Disposable {
    private static final float MIN_REPEAT_GAP = 0.05f;
    private static final float FADE_SECONDS = 0.7f;
    /** Autoplay demo runs quietly. */
    private static final float GAIN = com.AngryHomies.AngryBirds.Demo.enabled() ? 0.15f : 1f;

    private final AssetManager assets;
    private final Settings settings;
    private final ObjectFloatMap<Sfx> lastPlayed = new ObjectFloatMap<>();
    private boolean loaded;
    private float clock;

    private Music music;
    private float musicLevel;
    private float musicTarget;
    private Music outgoing;
    private float outgoingLevel;

    public AudioManager(AssetManager assets, Settings settings) {
        this.assets = assets;
        this.settings = settings;
    }

    public void onAssetsLoaded() {
        loaded = true;
    }

    public void play(Sfx sfx) {
        play(sfx, 1f, 0.06f);
    }

    /**
     * @param volume      0..1 loudness for this play (e.g. scaled by impact strength)
     * @param pitchJitter random pitch range around 1.0
     */
    public void play(Sfx sfx, float volume, float pitchJitter) {
        playPitched(sfx, volume, 1f + MathUtils.random(-pitchJitter, pitchJitter));
    }

    public void playPitched(Sfx sfx, float volume, float pitch) {
        if (!loaded || clock - lastPlayed.get(sfx, -1f) < MIN_REPEAT_GAP) {
            return;
        }
        float level = MathUtils.clamp(volume, 0f, 1f) * sfx.volume() * settings.sfxVolume() * settings.masterVolume() * GAIN;
        if (level < 0.01f) {
            return;
        }
        assets.get(sfx.path(), Sound.class).play(level, MathUtils.clamp(pitch, 0.5f, 2f), 0f);
        lastPlayed.put(sfx, clock);
    }

    /** Crossfades to the given track; asking for the track already playing only adjusts its level. */
    public void playMusic(AssetDescriptor<Music> track, float level) {
        if (!loaded) {
            return;
        }
        Music next = assets.get(track);
        musicTarget = level;
        if (next == music) {
            return;
        }
        if (outgoing != null) {
            outgoing.stop();
        }
        outgoing = music;
        outgoingLevel = musicLevel;
        music = next;
        musicLevel = 0f;
        music.setLooping(true);
        music.setVolume(0f);
        music.play();
    }

    public void stopMusic() {
        musicTarget = 0f;
    }

    public void update(float delta) {
        clock += delta;
        float step = delta / FADE_SECONDS;
        if (outgoing != null) {
            outgoingLevel -= step;
            if (outgoingLevel <= 0f) {
                outgoing.stop();
                outgoing = null;
            } else {
                outgoing.setVolume(outgoingLevel * musicGain());
            }
        }
        if (music != null) {
            musicLevel = MathUtils.clamp(musicLevel + Math.signum(musicTarget - musicLevel) * step,
                Math.min(musicLevel, musicTarget), Math.max(musicLevel, musicTarget));
            music.setVolume(musicLevel * musicGain());
        }
    }

    private float musicGain() {
        return settings.musicVolume() * settings.masterVolume() * GAIN;
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.stop();
        }
        if (outgoing != null) {
            outgoing.stop();
        }
    }
}
