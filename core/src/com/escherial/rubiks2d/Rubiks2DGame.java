package com.escherial.rubiks2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.escherial.rubiks2d.entities.RubikField;

public class Rubiks2DGame extends ApplicationAdapter {
    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public AssetManager manager = new AssetManager();

    public RubikField field;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1.0f, 1.0f, 1.0f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 0f, 40f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        /*
        camController = new CameraInputController(cam);
        camController.translateButton = -1;
        camController.rotateButton = Input.Buttons.RIGHT;
        Gdx.input.setInputProcessor(camController);
        */

        // game entities
        manager.load("models/tile/tile.g3db", Model.class);
        manager.finishLoading();

        field = new RubikField(3, 3, manager);
    }

    @Override
    public void render() {
        // camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(field.getBoxes(), environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        manager.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
