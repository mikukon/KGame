package com.r3studio.KGame.gl;

/**
 * Created by double on 16/3/7.
 */
public abstract class GLActor {
    private float marqueeLeft;
    public abstract void setX(float x);
    public abstract float getX();
    public abstract void setY(float y);
    public abstract float getY();
    public abstract void setPosition(float x, float y);
    public abstract float getWidth();
    public abstract float getHeight();
    public abstract float getScaleX();
    public abstract void setScaleX(float scaleX);
    public abstract float getScaleY();
    public abstract void setScaleY(float scaleY);
    public abstract void setScale(float scaleX, float scaleY);
    public abstract float getRotateAngle();
    public abstract void setRotateAngle(float angle);
    public abstract float getAlpha();
    public abstract void setAlpha(float alpha);
    public abstract void paint();
    public abstract void dispose();

    public float getMarqueeLeft() {
        return marqueeLeft * getScaleX();
    }

    public void setMarqueeLeft(float marqueeLeft) {
        this.marqueeLeft = marqueeLeft;
    }
}
