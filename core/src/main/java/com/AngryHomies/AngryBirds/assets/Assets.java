package com.AngryHomies.AngryBirds.assets;

import com.AngryHomies.AngryBirds.audio.Sfx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Every asset path in one place. Screens ask for descriptors, never raw strings, so a renamed
 * file breaks at one line instead of at runtime somewhere in a level.
 */
public final class Assets {
    private static final TextureLoader.TextureParameter SMOOTH = smooth();

    // Loading screen (loaded synchronously before anything else)
    public static final AssetDescriptor<Texture> LOADING_BG = tex("images/loading_screen.png");
    public static final AssetDescriptor<Texture> RED_BIRD = tex("images/red_bird.png");

    // Menus
    public static final AssetDescriptor<Texture> HOME_BG = tex("images/angry_bird_image.png");
    public static final AssetDescriptor<Texture> LOGO = tex("images/logo.png");
    public static final AssetDescriptor<Texture> LEVEL_SELECT_BG = tex("images/level_bg.png");
    public static final AssetDescriptor<Texture> LEVEL_SELECT_TITLE = tex("images/lvl_select_logo.png");
    public static final AssetDescriptor<Texture> LOAD_BG = tex("images/load_bg.png");
    public static final AssetDescriptor<Texture> BACK_BUTTON = tex("images/back_button.png");
    public static final AssetDescriptor<Texture> LEVEL_1 = tex("images/lvl1.png");
    public static final AssetDescriptor<Texture> LEVEL_2 = tex("images/lvl2.png");
    public static final AssetDescriptor<Texture> LEVEL_3 = tex("images/lvl3.png");
    public static final AssetDescriptor<Texture> WIN_ART = tex("images/win_screen.png");
    public static final AssetDescriptor<Texture> LOSE_ART = tex("images/lose_screen.png");
    public static final AssetDescriptor<Texture> PAUSE_ICON = tex("images/pause_icon.png");

    // Gameplay
    public static final AssetDescriptor<Texture> LEVEL_BG = tex("images/ground.png");
    public static final AssetDescriptor<Texture> CATAPULT = tex("images/catapult.png");
    public static final AssetDescriptor<Texture> YELLOW_BIRD = tex("images/yellow_bird.png");
    public static final AssetDescriptor<Texture> BLACK_BIRD = tex("images/black_bird.png");
    public static final AssetDescriptor<Texture> RED_ATTACK = tex("images/red_attack.png");
    public static final AssetDescriptor<Texture> YELLOW_SPEED = tex("images/yellow_speed.png");
    public static final AssetDescriptor<Texture> BLACK_BOMB = tex("images/Black_bomb.png");
    public static final AssetDescriptor<Texture> SMALL_PIG = tex("images/small_pig.png");
    public static final AssetDescriptor<Texture> BIG_PIG = tex("images/big_pig.png");
    public static final AssetDescriptor<Texture> KING_PIG = tex("images/king_pig.png");

    /** Block art is chosen per block in the level JSON, so these are looked up by file name. */
    public static final String[] BLOCK_TEXTURES = {
        "block.png", "glass_block.png", "sp_glassblock.png", "stone_blocks.png",
        "st_stoneblock.png", "standing_woodblock.png", "sleepingwooden_block.png", "stone_block.png",
    };

    public static final AssetDescriptor<Skin> SKIN = new AssetDescriptor<>(
        "uiskin2/freezing-ui.json", Skin.class, new SkinLoader.SkinParameter("uiskin2/freezing-ui.atlas"));

    public static final AssetDescriptor<Music> MENU_MUSIC = new AssetDescriptor<>("audio/loading_music.mp3", Music.class);
    public static final AssetDescriptor<Music> AMBIENCE = new AssetDescriptor<>("audio/sfx/ambience.wav", Music.class);

    private Assets() {
    }

    /** Assets the loading screen itself needs, loaded before the progress bar can draw. */
    public static void loadBootstrap(AssetManager manager) {
        manager.load(LOADING_BG);
        manager.load(RED_BIRD);
        manager.finishLoading();
    }

    /** Queues everything else; the loading screen drives {@link AssetManager#update(int)}. */
    public static void queueAll(AssetManager manager) {
        for (AssetDescriptor<Texture> descriptor : new AssetDescriptor[] {
            HOME_BG, LOGO, LEVEL_SELECT_BG, LEVEL_SELECT_TITLE, LOAD_BG, BACK_BUTTON, LEVEL_1, LEVEL_2,
            LEVEL_3, WIN_ART, LOSE_ART, PAUSE_ICON, LEVEL_BG, CATAPULT, YELLOW_BIRD, BLACK_BIRD,
            RED_ATTACK, YELLOW_SPEED, BLACK_BOMB, SMALL_PIG, BIG_PIG, KING_PIG,
        }) {
            manager.load(descriptor);
        }
        for (String file : BLOCK_TEXTURES) {
            manager.load(blockPath(file), Texture.class, SMOOTH);
        }
        for (Sfx sfx : Sfx.values()) {
            manager.load(sfx.path(), Sound.class);
        }
        manager.load(SKIN);
        manager.load(MENU_MUSIC);
        manager.load(AMBIENCE);
    }

    public static Texture block(AssetManager manager, String file) {
        return manager.get(blockPath(file), Texture.class);
    }

    public static AssetDescriptor<Texture> levelThumb(int level) {
        switch (level) {
            case 1:
                return LEVEL_1;
            case 2:
                return LEVEL_2;
            default:
                return LEVEL_3;
        }
    }

    private static String blockPath(String file) {
        return "images/" + file;
    }

    private static AssetDescriptor<Texture> tex(String path) {
        return new AssetDescriptor<>(path, Texture.class, SMOOTH);
    }

    private static TextureLoader.TextureParameter smooth() {
        TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
        // Mipmaps keep the large 1-2k source images crisp when scaled down to world size.
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        return parameter;
    }
}
