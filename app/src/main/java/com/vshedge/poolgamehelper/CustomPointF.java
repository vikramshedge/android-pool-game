package com.vshedge.poolgamehelper;

import android.graphics.PointF;

public class CustomPointF extends PointF {
    private float x;
    private float y;

    public CustomPointF(float x, float y) {
        super(x, y);
        this.x = x;
        this.y = y;
    }

    public CustomPointF(PointF pointF) {
        super(pointF.x, pointF.y);
        this.x = pointF.x;
        this.y = pointF.y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public CustomPointF duplicate() {
        return new CustomPointF(this.getX(), this.getY());
    }

    public String printCords() {
        return this.getX() + ", " + this.getY();
    }

}
