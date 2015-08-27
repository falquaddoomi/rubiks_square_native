package com.escherial.rubiks2d.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.escherial.rubiks2d.Rubiks2DGame;
import com.escherial.rubiks2d.state.PivotMove;
import com.escherial.rubiks2d.state.RubikField;
import com.escherial.rubiks2d.state.RubikSquare;
import com.escherial.rubiks2d.strategies.scramblers.RandomScrambler;
import com.escherial.rubiks2d.strategies.scramblers.ScramblerProvider;

/**
 * Created by Faisal on 8/20/2015.
 */
public class GameScreen implements Screen, InputProcessor {
    // camera/rendering/asset management stuff
    public Environment environment;
    public final PerspectiveCamera cam;
    public final OrthographicCamera orthoCam;
    public CameraInputController camController;
    public ModelBatch modelBatch;

    // game entities/constants
    public RubikField field;
    private static final int MOVE_FRAMES = 15;
    private int level = 1;
    private int running_total = 0;

    private ScramblerProvider scrambler = new RandomScrambler();

    // for game input (choosing rows/cols to pivot)
    private Vector3 position = new Vector3();
    private boolean shiftDown = false;
    private RubikSquare dragging = null;
    private Vector2 lastClick = new Vector2();
    private Vector2 curDragPos = new Vector2();

    // for rendering text
    private ScreenViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    // for rendering HUD stuff
    private Stage stage;
    private final Table table;
    private final Label levelLabel, scoreLabel, totalLabel;

    // used for determining where to draw the reticule
    private Plane gamePlane =  new Plane(new Vector3(0, 0, 1), 0.0f);
    private Vector3 intersect = new Vector3();
    private Rubiks2DGame game;

    public GameScreen(final Rubiks2DGame game) {
        this.game = game;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1.0f, 1.0f, 1.0f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        /*
        camController = new CameraInputController(cam);
        camController.translateButton = -1;
        camController.rotateButton = Input.Buttons.RIGHT;
        Gdx.input.setInputProcessor(camController);
        */

        field = new RubikField(level, game, this);
        scrambler.scramble(field, level);

        // create the cam and make sure the field is in view
        cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // cam.up.set(0, 1, 0);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 800f;
        refreshCamDist();

        // maintains aspect ratio when the screen changes dimensions
        viewport = new ScreenViewport(cam);

        // create an orthographic camera for HUD stuff
        orthoCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthoCam.update();

        // set up the stage
        stage = new Stage(new ScreenViewport(orthoCam));
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // create all labels
        table.left().top();
        levelLabel = new Label("Level: " + level, game.skin, "default", Color.WHITE);
        table.add(levelLabel).left().pad(10.0f, 10.0f, 0.0f, 0.0f); table.row();

        scoreLabel = new Label("Score: 0", game.skin, "default", Color.WHITE);
        table.add(scoreLabel).left().padLeft(10.0f); table.row();

        totalLabel = new Label("Total: 0", game.skin, "default", Color.WHITE);
        table.add(totalLabel).left().padLeft(10.0f); table.row();

        // and for rendering the selection reticule
        shapeRenderer = new ShapeRenderer();

        // attach controls
        Gdx.input.setInputProcessor(new InputMultiplexer(this));

        // disable continuous rendering, since most of the time nothing is happening
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    // =======================================================================================
    // === application-level events
    // =======================================================================================

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        // update the field
        field.update();

        // camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cam.update();

        modelBatch.begin(cam);
        modelBatch.render(field, environment);
        modelBatch.end();

        // draw the reticule as well
        if (dragging != null) {
            int dist = (int)(lastClick.cpy().sub(curDragPos).len());

            shapeRenderer.setProjectionMatrix(orthoCam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 1, 1, 0.25f);
            shapeRenderer.circle(lastClick.x, lastClick.y, dist * 0.6f, 32);
            shapeRenderer.setColor(0.04f, 0.2f, 0.4f, 0.25f);
            shapeRenderer.circle(lastClick.x, lastClick.y, dist * 0.3f, 32);
            shapeRenderer.end();
        }

        /*
        batch.begin();
        batch.setProjectionMatrix(orthoCam.combined);
        int offset = (int)game.regFont.getLineHeight();
        int lineHeight = (int)(game.regFont.getLineHeight());
        game.regFont.draw(batch, "Level: " + level, offset, offset);
        game.regFont.draw(batch, "Score: " + (field.getInitialPivots() - field.getMoves()), offset, offset + lineHeight);
        game.regFont.draw(batch, "Total: " + running_total, offset, offset + lineHeight * 2);
        batch.end();
         */

        // debug render the table
        table.drawDebug(shapeRenderer);

        // draw the stage, too
        stage.act();
        stage.draw();

        // and run another one
        if (field.isAnimating())
            Gdx.graphics.requestRendering();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        font.dispose();
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        stage.getViewport().update(width, height, true);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    // =======================================================================================
    // === input handling
    // =======================================================================================

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        dragging = getObjectByBounds(screenX, screenY);

        if (dragging != null) {
            Vector3 proj = orthoCam.unproject(new Vector3(screenX, screenY, 0));
            lastClick.set(proj.x, proj.y);
            curDragPos.set(proj.x, proj.y);

            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (dragging != null) {
            // determine direction of drag
            Vector3 proj = orthoCam.unproject(new Vector3(screenX, screenY, 0));
            float dir = lastClick.sub(new Vector2(proj.x, proj.y)).angle();

            boolean isRow = ((dir > 315 || dir < 45) || (dir > 135 && dir < 225)) && !shiftDown;
            boolean clockwise = (dir > 315 || dir < 45) || (dir > 45 && dir < 135);

            // deselect row...
            // field.setSelection((isRow)?dragging.i:dragging.j, isRow, false);
            // ...and push the move
            field.pushPivot(new PivotMove((isRow) ? dragging.i : dragging.j, isRow, clockwise, false, MOVE_FRAMES));
            dragging = null;

            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        /*
        if (dragging != null) {
            // determine direction of drag
            float dir = lastClick.sub(new Vector2(screenX, screenY)).angle();

            boolean isRow = ((dir > 315 || dir < 45) || (dir > 135 && dir < 225)) && !shiftDown;
            boolean clockwise = (dir > 315 || dir < 45) || (dir > 45 && dir < 135);

            // colorize selected row/col on the board
            // field.setSelection((isRow)?dragging.i:dragging.j, isRow, true);

            return true;
        }
        */

        if (dragging != null) {
            Vector3 proj = orthoCam.unproject(new Vector3(screenX, screenY, 0));
            curDragPos.set(proj.x, proj.y);
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SHIFT_LEFT)
            shiftDown = true;

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SHIFT_LEFT)
            shiftDown = false;
        else if (keycode == Input.Keys.I) {
            if (shiftDown)
                levelComplete();
            else
                field.startVictory();
        }
        else if (keycode == Input.Keys.ESCAPE)
            Gdx.app.exit();

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    public RubikSquare getObjectByBounds(int screenX, int screenY) {
        Ray ray = cam.getPickRay(screenX, screenY);

        for (final RubikSquare instance : field.getBoxes()) {
            BoundingBox b = new BoundingBox(RubikSquare.bounds);
            b.mul(instance.transform);

            if (Intersector.intersectRayBoundsFast(ray, b))
                return instance;
        }

        return null;
    }

    // =======================================================================================
    // === events raised by the game field
    // =======================================================================================

    public void moveComplete() {
        scoreLabel.setText("Score: " + (field.getInitialPivots() - field.getMoves()));
    }

    public void levelComplete() {
        level += 1;
        running_total += (field.getInitialPivots() - field.getMoves());

        // update level and total labels
        levelLabel.setText("Level: " + level);
        totalLabel.setText("Total: " + running_total);

        // trash and recreate the board
        field = new RubikField(level, game, this);
        scrambler.scramble(field, level);
        refreshCamDist();

        // and make sure we start rendering it
        Gdx.graphics.requestRendering();
    }

    // =======================================================================================
    // === view state management
    // =======================================================================================

    public void refreshCamDist() {
        cam.position.set(0f, 0f, Math.max(field.rows, field.cols)*RubikSquare.WIDTH*3 + 10.0f);
        cam.update();
    }
}
