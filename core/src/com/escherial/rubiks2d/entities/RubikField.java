package com.escherial.rubiks2d.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.ArrayList;

/**
 * Created by Faisal on 6/28/2015.
 */
public class RubikField {
    ArrayList<ModelInstance> boxes = new ArrayList<ModelInstance>();
    boolean faces[][];

    int rows, cols;
    final float WIDTH = 6.5f, HEIGHT = 6.5f, PADDING = 0.5f, DEPTH = 1;

    ArrayList<PivotMove> pending_moves = new ArrayList<PivotMove>();
    PivotMove curmove;

    public RubikField(int rows, int cols, AssetManager manager) {
        this.rows = rows;
        this.cols = cols;
        faces = new boolean[rows][cols];

        // create models for all of the faces
        ModelBuilder modelBuilder = new ModelBuilder();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Model m = manager.get("models/tile/tile.g3db", Model.class);
                ModelInstance instance = new ModelInstance(m);
                instance.transform.setToTranslation(j*WIDTH - (rows*WIDTH)/2.0f, i*HEIGHT - (cols*HEIGHT)/2.0f, 0);
                instance.transform.translate(WIDTH / 2, HEIGHT / 2, DEPTH / 2);

                if (faces[i][j]) {
                    instance.transform.rotate(0f, 1f, 0f, 180);
                }

                boxes.add(instance);
            }
        }
    }

    public ArrayList<ModelInstance> getBoxes() {
        return boxes;
    }

    // ===============================================================
    // === rendering updates
    // ===============================================================

    void update() {
        // if we're in a pivot state, rotate the tiles that are involved
        if (curmove != null) {
            if (curmove.isRow) {
                for (int i = 0; i < cols; i++) {

                }
            }
        }
    }

    // ===============================================================
    // === game transformations
    // ===============================================================

    void performPivot(PivotMove curmove) {
        if (!curmove.isRow) {
            for (int i = 0; i <= rows/2; i++) {
                boolean tmp = faces[i][curmove.id];
                faces[i][curmove.id] = !faces[rows-1-i][curmove.id];
                faces[rows-1-i][curmove.id] = tmp;
            }
        } else {
            for (int i = 0; i <= cols/2; i++) {
                boolean tmp = faces[curmove.id][i];
                faces[curmove.id][i] = !faces[curmove.id][cols-1-i];
                faces[curmove.id][cols-1-i] = tmp;
            }
        }

        // check for victory
        if (!curmove.auto && checkVictory()) {
            // victory!
        }
    }

    // ===============================================================
    // === game state verification
    // ===============================================================

    boolean checkVictory() {
        // and check for victory
        int reds = 0, whites = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (faces[i][j]) {
                    reds += 1;
                } else {
                    whites += 1;
                }
            }
        }

        return (reds == rows*cols || whites == rows*cols);
    }
}
