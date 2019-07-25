package org.tensorflow.lite.examples.detection.convertion;

public class Box {

    float x,y,w,h,confidence;

    public Box(){}
    Box(float x,float y,float w, float h)
    {
        this.x=x;
        this.y = y;
        this.w = w;
        this.h=h;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
