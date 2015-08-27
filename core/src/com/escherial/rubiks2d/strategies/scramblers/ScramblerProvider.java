package com.escherial.rubiks2d.strategies.scramblers;

import com.escherial.rubiks2d.state.RubikField;

/**
 * Created by Faisal on 8/23/2015.
 */
public interface ScramblerProvider {
    public void scramble(RubikField field, int level);
}
