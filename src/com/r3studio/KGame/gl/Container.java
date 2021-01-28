package com.r3studio.KGame.gl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by double on 16/3/7.
 */
public class Container extends GLActor {
    private float x;
    private float y;
    private float w;
    private float h;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    List<GLActor> actors = new ArrayList<GLActor>();
    public void addActor(GLActor actor){
        if (!actors.contains(actor)){
            actor.setPosition(actor.getX() + x, actor.getY() + y);
            actors.add(actor);
        }
    }

    public void removeActor(GLActor actor){
        actors.remove(actor);
    }

    @Override
    public void setX(float x) {
        setPosition(x, this.y);
    }

    @Override
    public float getX() {
        return 0;
    }

    @Override
    public void setY(float y) {

    }

    @Override
    public float getY() {
        return 0;
    }

    @Override
    public void setPosition(float x, float y) {
        for (GLActor actor:actors) {
            actor.setX(actor.getX() - this.x + x);
            actor.setY(actor.getY() - this.y + y);
        }

        this.x = x;
        this.y = y;
    }

    @Override
    public float getWidth() {
        return this.w;
    }

    @Override
    public float getHeight() {
        return this.h;
    }

    public void setWidth(float width){
        this.w = width;
    }

    public void setHeight(float height){
        this.h = height;
    }

    @Override
    public float getScaleX() {
        return 0;
    }

    @Override
    public void setScaleX(float scaleX) {

    }

    @Override
    public float getScaleY() {
        return 0;
    }

    @Override
    public void setScaleY(float scaleY) {

    }

    @Override
    public void setScale(float scaleX, float scaleY) {

    }

    @Override
    public float getRotateAngle() {
        return 0;
    }

    @Override
    public void setRotateAngle(float angle) {

    }

    @Override
    public float getAlpha() {
        return 0;
    }

    @Override
    public void setAlpha(float alpha) {

    }

    @Override
    public void paint() {
        for (GLActor actor:actors) {
            actor.paint();
        }
    }

    @Override
    public void dispose() {
        for (GLActor actor:actors) {
            actor.dispose();
        }

        actors.clear();
    }

    public List<GLActor> getActors(){
        return actors;
    }
}
