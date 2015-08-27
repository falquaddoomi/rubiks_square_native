package com.escherial.rubiks2d.strategies.scramblers;

import com.escherial.rubiks2d.state.PivotMove;
import com.escherial.rubiks2d.state.RubikField;

/**
 * Created by Faisal on 8/23/2015.
 */
public class RandomScrambler implements ScramblerProvider {
    private int initial_pivots;

    @Override
    public void scramble(RubikField field, int level) {
        // randomize the board
        boolean isRow = true;
        initial_pivots = (int)(level*1.5);
        for (int i = 0; i < initial_pivots; i++) {

            int duration = (int)((level <= 6 || initial_pivots-i <= 4)?15 - (level*0.5):0);
            int id = (isRow)?(int)(Math.random() * field.rows):(int)(Math.random() * field.cols);
            field.pushPivot(new PivotMove(id, isRow, (Math.random() > 0.3), true, duration));

            // toggle the row/col flip
            isRow = !isRow;
        }
    }
}
