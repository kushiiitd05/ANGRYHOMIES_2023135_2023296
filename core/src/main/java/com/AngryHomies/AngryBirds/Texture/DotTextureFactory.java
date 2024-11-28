package com.AngryHomies.AngryBirds.Texture;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class DotTextureFactory implements TextureFactory {
    @Override
    public Texture createSlingShotTexture() {
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(4, 4, 4);
        Texture dotTexture = new Texture(pixmap);
        pixmap.dispose();
        return dotTexture;
    }
}

