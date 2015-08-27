package com.escherial.rubiks2d.strategies.scorers;

import com.escherial.rubiks2d.state.PivotMove;
import com.escherial.rubiks2d.state.RubikField;

/**
 * Created by Faisal on 8/24/2015.
 */
public interface ScoreProvider {
    public int applyMove(RubikField board, PivotMove move);
}
