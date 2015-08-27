package com.escherial.rubiks2d.state;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Faisal on 8/5/2015.
 */
public class RubikSquare extends ModelInstance {
    public static float WIDTH = 6.5f, HEIGHT = 6.5f, PADDING = 0.5f, DEPTH = 1;

    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public final float radius;
    public int i, j;
    public boolean facing;

    public Matrix4 localTransform;

    private static boolean boundsSet = false;
    public final static BoundingBox bounds = new BoundingBox();

    private final RubikField parent;

    public RubikSquare(RubikField parent, Model m, boolean facing, int i, int j) {
        super(m);

        this.parent = parent;
        this.i = i;
        this.j = j;
        this.localTransform = new Matrix4().idt();

        if (!boundsSet) {
            calculateBoundingBox(bounds);
            boundsSet = true;
        }
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;

        // set its orientation as well, and sync its position, etc.
        this.facing = facing;
        syncRestingPosition();
    }

    public boolean isRed() {
        return facing;
    }

    public void flip() {
        facing = !facing;

        // synchronize translation/rotation state
        syncRestingPosition();
    }

    // pivot animating state

    public void pivot(boolean rowRotate, float degrees, boolean clockwise) {
        localTransform.setToTranslation(0, 0, 0);

        // same as syncRestingPosition except that we perform this rotation in the middle
        if (rowRotate)
            localTransform.rotate(0, 1, 0, degrees * (clockwise?-1.0f:1.0f));
        else
            localTransform.rotate(1, 0, 0, degrees * (clockwise?-1.0f:1.0f));

        // and do the placement localTransform
        syncTranslation();
        syncFacing();
    }

    // idle and animating states

    public void syncEnteringPosition(float lerp) {
        localTransform.setToTranslation(0, 0, 0);
        syncTranslation();
        localTransform.translate(0, (1.0f - lerp) * 30.0f, 0);
    }

    public void syncRestingPosition() {
        localTransform.setToTranslation(0, 0, 0);
        syncTranslation();
        syncFacing();
    }

    public void syncVictoryPosition(float lerp) {
        localTransform.setToTranslation(0, 0, 0);
        /*
        localTransform.translate((float) j * WIDTH - ((parent.cols * WIDTH) / 2.0f), (float) i * HEIGHT - ((parent.rows * HEIGHT) / 2.0f), 0);
        localTransform.translate(WIDTH / 2.0f, HEIGHT / 2.0f, DEPTH / 2.0f);
        */
        syncTranslation();

        float lerp2 = lerp*lerp;

        localTransform.translate(20.0f * (j - parent.cols / 2) * lerp2, 20.0f * (i - parent.rows / 2) * lerp2, 0.0f);

        syncFacing();
        localTransform.rotate(0.2f, 0.8f, 0, 0.5f * 360.0f * lerp2);
        localTransform.rotate(0.4f, 0.4f, 0.2f, 0.8f * 360.0f * lerp2);
    }

    // helpers for setting up localTransformation state

    private void syncTranslation() {
        localTransform.translate((float) j * WIDTH - ((parent.cols * WIDTH) / 2.0f), (float) i * HEIGHT - ((parent.rows * HEIGHT) / 2.0f), 0);
        localTransform.translate(WIDTH / 2, HEIGHT / 2, DEPTH / 2);
    }

    private void syncFacing() {
        if (facing) {
            localTransform.rotate(0f, 1f, 0f, 180);
        }
    }
}
