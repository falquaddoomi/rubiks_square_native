package com.escherial.rubiks2d;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.escherial.rubiks2d.screens.GameScreen;

public class Rubiks2DGame extends Game {
    public AssetManager manager = new AssetManager();
    public Skin skin;

    public void create() {
        // game entities
        manager.load("models/tile/tile.g3db", Model.class);
        manager.finishLoading();

        // load the skin, too
        skin = new Skin(Gdx.files.internal("skin.json"));

        this.setScreen(new GameScreen(this));
    }

    public void render() {
        // delegates rendering to the active screen
        super.render();
    }

    public void dispose() {
        skin.dispose();
        manager.dispose();
    }
}
