package com.escherial.rubiks2d.entities;

/**
 * Created by Faisal on 7/1/2015.
 */
public class PivotMove {
    int id;
    boolean isRow;
    boolean clockwise;
    int frames;
    boolean auto;
    int duration;

    PivotMove(int id, boolean isRow, boolean clockwise, boolean auto, int duration) {
        this.id = id;
        this.isRow = isRow;
        this.clockwise = clockwise;
        this.frames = duration;
        this.duration = duration;
        this.auto = auto;
    }
}
