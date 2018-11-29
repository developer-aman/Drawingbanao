package com.agtechnosolution.drawingbanao.POJO;

import android.graphics.Path;

import io.realm.RealmObject;

/**
 * Created by AnujPc on 26-11-2018.
 */

public class FingerPath {
    public int color;
    public int strokeWidth;
    public Path path;

    public FingerPath(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

}
