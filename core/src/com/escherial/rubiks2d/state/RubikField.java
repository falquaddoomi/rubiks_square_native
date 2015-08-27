package com.escherial.rubiks2d.state;

import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.escherial.rubiks2d.Rubiks2DGame;
import com.escherial.rubiks2d.screens.GameScreen;

import java.util.ArrayList;

/**
 * Created by Faisal on 6/28/2015.
 */
public class RubikField implements RenderableProvider {
    private final Rubiks2DGame game;
    private final GameScreen parent;
    private ArrayList<RubikSquare> boxes = new ArrayList<RubikSquare>();
    private RubikSquare faces[][];

    public final int rows, cols;

    private ArrayList<PivotMove> pending_moves = new ArrayList<PivotMove>();
    private PivotMove curmove;
    private int moves_so_far = 0;
    private int initial_pivots = 0;

    // animation control
    private int entrance_frames = 0;
    private static final int ENTRANCE_DURATION = 20;
    private int victory_frames = 0;
    private static final int VICTORY_DURATION = 80;

    public Matrix4 localTransform = new Matrix4().idt();

    public RubikField(int rows, int cols, Rubiks2DGame game, GameScreen parent) {
        this.game = game;
        this.parent = parent;
        this.rows = rows;
        this.cols = cols;

        faces = new RubikSquare[rows][cols];

        // create models for all of the faces
        Model m = this.game.manager.get("models/tile/tile.g3db", Model.class);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                RubikSquare instance = new RubikSquare(this, m, true, i, j);
                faces[i][j] = instance;

                // the below line would show the red side of the box
                // instance.transform.rotate(0f, 1f, 0f, 180);

                boxes.add(instance);
            }
        }

        // and animate it coming in
        entrance_frames = ENTRANCE_DURATION;

        for (RubikSquare box : boxes)
            box.syncEnteringPosition(0.0f);
    }

    public RubikField(int level, Rubiks2DGame game, GameScreen parent) {
        // rows, cols, pivots = (int)(level/3)+3, (int)(level/3)+3, level*1.5
        this(
                (int) (level / 3) + 3, // rows scaled w/level
                (int) (level / 3) + 3, // cols scaled w/level
                game, parent
        );
    }

    // ===============================================================
    // === accessors
    // ===============================================================

    public ArrayList<RubikSquare> getBoxes() {
        return boxes;
    }

    public int getMoves() {
        return moves_so_far;
    }

    public int getInitialPivots() {
        return initial_pivots;
    }

    // ===============================================================
    // === game move requests
    // ===============================================================

    public void pushPivot(PivotMove move) {
        // if we're in a state where we can accept moves, accept it!
        pending_moves.add(move);
    }

    /*
    private void colorizeSquare(RubikSquare candidate, boolean enable) {
        if (enable)
            candidate.materials.get(0).set(ColorAttribute.createDiffuse(0.0f, 0.5f, 0.5f, 0.25f));
        else
            candidate.materials.get(0).remove(ColorAttribute.Diffuse);
    }

    public void setSelection(int id, boolean isRow, boolean enable) {
        if (isRow) {
            for (int i = 0; i < cols; i++)
                colorizeSquare(faces[id][i], enable);
        }
        else {
            for (int i = 0; i < rows; i++)
                colorizeSquare(faces[i][id], enable);
        }
    }
    */

    // ===============================================================
    // === rendering updates
    // ===============================================================

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        for (RubikSquare box : boxes) {
            box.transform.set(this.localTransform).mul(box.localTransform);

            // adds all the renderables in this box to the renderable list
            box.getRenderables(renderables, pool);
        }
    }

    public void update() {
        if (entrance_frames > 0) {
            float lerp = 1.0f - (entrance_frames/(float)ENTRANCE_DURATION);

            // shift view so the board descends from the top
            for (RubikSquare box : boxes)
                box.syncEnteringPosition(lerp);

            entrance_frames -= 1;

            if (entrance_frames == 0) {
                for (RubikSquare box : boxes)
                    box.syncRestingPosition();
            }
        }
        else if (victory_frames > 0) {
            // top priority is animating the victory frames, then triggering a level change when they're done
            float lerp = 1.0f - (victory_frames/(float)VICTORY_DURATION);

            // update every box
            for (RubikSquare box : boxes)
                box.syncVictoryPosition(lerp);

            victory_frames -= 1;

            if (victory_frames == 0) {
                // trigger the level completion in the game mgr, which should destroy us
                parent.levelComplete();
            }
        }
        else if (curmove != null) {
            // if we're in a pivot state, rotate the tiles that are involved
            if (curmove.frames > 0) {
                float lerp = 1.0f - (curmove.frames/(float)curmove.duration);
                float degs = 180.0f/(float)curmove.duration;

                if (curmove.isRow) {
                    for (int i = 0; i < cols; i++) {
                        // compute rotations for the entire row
                        faces[curmove.id][i].pivot(true, 180.0f * lerp, curmove.clockwise);
                        // faces[curmove.id][i].transform.rotate(0, 1, 0, degs);
                    }
                } else {
                    for (int i = 0; i < rows; i++) {
                        // compute rotations for the column
                        faces[i][curmove.id].pivot(false, 180.0f * lerp, curmove.clockwise);
                        // faces[i][curmove.id].transform.rotate(1, 0, 0, degs);
                    }
                }

                curmove.frames -= 1;
            }
            else {
                // this is the last movement frame, so perform the pivot and make curmove null
                performPivot(curmove);

                // record moves the player has made
                if (!curmove.auto)
                    moves_so_far += 1;

                // notify game that the move is done
                parent.moveComplete();

                curmove = null;
            }
        }
        else if (!pending_moves.isEmpty()) {
            // unshift the least recent move from the queue
            curmove = pending_moves.remove(0);
        }
    }

    // ===============================================================
    // === game transformations
    // ===============================================================

    void performPivot(PivotMove curmove) {
        if (!curmove.isRow) {
            for (int i = 0; i < rows/2; i++) {
                RubikSquare tmp = faces[i][curmove.id];
                faces[i][curmove.id] = faces[rows-1-i][curmove.id];
                faces[rows-1-i][curmove.id] = tmp;

                // perform the coordinate swap and face toggle
                int old_row = faces[i][curmove.id].i;
                faces[i][curmove.id].i = faces[rows-1-i][curmove.id].i;
                faces[rows-1-i][curmove.id].i = old_row;

                faces[i][curmove.id].flip();
                faces[rows-1-i][curmove.id].flip();
            }

            // for odd # of columns, be sure to flip the middle
            if (rows % 2 != 0)
                faces[rows/2][curmove.id].flip();

        } else {
            for (int i = 0; i < cols/2; i++) {
                RubikSquare tmp = faces[curmove.id][i];
                faces[curmove.id][i] = faces[curmove.id][cols-1-i];
                faces[curmove.id][cols-1-i] = tmp;

                // perform the coordinate swap and face toggle
                int old_col = faces[curmove.id][i].j;
                faces[curmove.id][i].j = faces[curmove.id][cols-1-i].j;
                faces[curmove.id][cols-1-i].j = old_col;

                faces[curmove.id][i].flip();
                faces[curmove.id][cols-1-i].flip();
            }

            // for odd # of columns, be sure to flip the middle
            if (cols % 2 != 0)
                faces[curmove.id][cols/2].flip();
        }

        // check for victory
        if (!curmove.auto && checkVictory()) {
            // victory!
            startVictory();
        }
    }

    // ===============================================================
    // === game state verification
    // ===============================================================

    public boolean checkVictory() {
        // and check for victory
        int reds = 0, whites = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (faces[i][j].isRed()) {
                    reds += 1;
                } else {
                    whites += 1;
                }
            }
        }

        return (reds == rows*cols || whites == rows*cols);
    }

    public void startVictory() {
        victory_frames = VICTORY_DURATION;
    }

    public boolean isAnimating() {
        return curmove != null || !pending_moves.isEmpty() || entrance_frames > 0 || victory_frames > 0;
    }
}
