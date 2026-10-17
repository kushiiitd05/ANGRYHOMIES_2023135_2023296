package com.AngryHomies.AngryBirds.audio;

/** Every sound effect, with its file and a base loudness so the mix is set in one place. */
public enum Sfx {
    SLING_STRETCH("audio/sfx/sling_stretch.wav", 0.55f),
    SLING_RELEASE("audio/sfx/sling_release.wav", 0.8f),
    WOOD_HIT("audio/sfx/wood_hit.wav", 0.7f),
    WOOD_BREAK("audio/sfx/wood_break.wav", 0.85f),
    GLASS_HIT("audio/sfx/glass_hit.wav", 0.55f),
    GLASS_BREAK("audio/sfx/glass_break.wav", 0.8f),
    STONE_HIT("audio/sfx/stone_hit.wav", 0.75f),
    STONE_BREAK("audio/sfx/stone_break.wav", 0.9f),
    PIG_HIT("audio/piglette oink a8.wav", 0.6f),
    PIG_POP("audio/sfx/pig_pop.wav", 0.9f),
    BIRD_THUD("audio/sfx/bird_thud.wav", 0.6f),
    BIRD_HOP("audio/sfx/bird_hop.wav", 0.45f),
    BATTLE_CRY("audio/red_attack.mp3", 0.8f),
    DASH("audio/sfx/dash.wav", 0.8f),
    WHIRL("audio/sfx/whirl.wav", 0.7f),
    SLASH("audio/sfx/slash.wav", 0.85f),
    FUSE("audio/sfx/fuse.wav", 0.6f),
    EXPLOSION("audio/sfx/explosion.wav", 1f),
    UI_CLICK("audio/sfx/ui_click.wav", 0.6f),
    UI_HOVER("audio/sfx/ui_hover.wav", 0.35f),
    WHOOSH("audio/sfx/whoosh.wav", 0.45f),
    STAR("audio/sfx/star.wav", 0.75f),
    SCORE_TICK("audio/sfx/score_tick.wav", 0.25f),
    WIN("audio/sfx/win.wav", 0.8f),
    LOSE("audio/sfx/lose.wav", 0.8f);

    private final String path;
    private final float volume;

    Sfx(String path, float volume) {
        this.path = path;
        this.volume = volume;
    }

    public String path() {
        return path;
    }

    public float volume() {
        return volume;
    }
}
