package com.example.kaelhosvalde.graphemax;

import android.graphics.Color;

public class ArcFinal extends Arc {

    private Node nodeTo;
    private float [] midPoint;
    private float [] tangent;
    private String etiquette;
    public int largeurEtiquette;
    private int color;
    private int width;
    public boolean hasBeenModified = false;

    public ArcFinal(Node _nodeFrom, Node _nodeTo, String _etiquette){
        super(_nodeFrom);
        this.nodeTo = _nodeTo;
        this.etiquette = _etiquette;
        this.color = Color.BLACK;
        this.width = 5;
        this.largeurEtiquette = 30;
    }

    public Node getNodeTo(){
        return this.nodeTo;
    }

    public String getEtiquette(){
        return this.etiquette;
    }

    public float[] getMidPoint() {
        return midPoint;
    }

    public void setMidPoint(float[] midPoint) {
        this.midPoint = midPoint;
    }

    public float[] getTangent() {
        return tangent;
    }

    public void setTangent(float[] tangent) {
        this.tangent = tangent;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public int getLargeurEtiquette() {
        return largeurEtiquette;
    }

    public void setMidPointCourb(float[] midPoint){
        hasBeenModified = true;
        this.setMidPoint(midPoint);
    } 


}
