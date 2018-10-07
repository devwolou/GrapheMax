package com.example.kaelhosvalde.graphemax;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.example.kaelhosvalde.graphemax.MainActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Graph {

    private Collection<Node> nodes;
    private Collection<ArcFinal> arcs;
    private ArcTemporaire arcTemp;


    public Graph(Point p){
        nodes = new ArrayList<Node>();
        arcs = new ArrayList<ArcFinal>();

        Log.i("SIZE GRAPH ",String.valueOf(p.x));
        Log.i("SIZE GRAPH ",String.valueOf(p.y));

        initialisationGraph();

    }

    public Collection<Node> getNodes() {
        return new ArrayList<Node>(nodes);
    }

    public void addNode(Node _n){
        nodes.add(_n);
    }

    public void removeNode(Node _n) {

        nodes.remove(_n);

        //On supprime également les arcs liés à ce noeud
        Iterator<ArcFinal> it = arcs.iterator();
        while (it.hasNext()) {
            ArcFinal a = it.next();
            if (a.getNodeTo() == _n || a.getNodeFrom() == _n) {
                it.remove();
            }
        }

    }

    public Node getOneNode(float x, float y) {
        for (Node n : nodes) {
            if (n.contains(x, y)) {
                return n;
            }
        }

        return null;
    }

    public ArcTemporaire getArcTemp() {
        return arcTemp;
    }


    public Collection<ArcFinal> getArcs(){
        return new ArrayList<ArcFinal>(arcs);
    }


    public void initArcTemp(float x, float y) {
        arcTemp = new ArcTemporaire(getOneNode(x, y));
    }

    public void setArcTemp(float x, float y) {
        arcTemp.setNodeX(x);
        arcTemp.setNodeY(y);
    }

    public void makeArcTempNull() {
        arcTemp = null;
    }
    public void addArc(ArcFinal a) {
        arcs.add(a);
    }

    public void removeArc(ArcFinal a) {
        arcs.remove(a);
    }

    public void initialisationGraph(){

        nodes.add(new Node(140,200, "1", Color.BLUE));
        nodes.add(new Node(360,200, "2", Color.BLUE));
        nodes.add(new Node(580,200, "3", Color.BLUE));
        nodes.add(new Node(140,450, "4", Color.BLUE));
        nodes.add(new Node(360,450, "5", Color.BLUE));
        nodes.add(new Node(580,450, "6", Color.BLUE));
        nodes.add(new Node(140,700, "7", Color.BLUE));
        nodes.add(new Node(360,700, "8", Color.BLUE));
        nodes.add(new Node(580,700, "9", Color.BLUE));

    }

    public ArcFinal getOneArc(float x, float y){
        RectF r;
        for (ArcFinal a : arcs){
            float xMid = a.getMidPoint()[0];
            float yMid = a.getMidPoint()[1];

            r = new RectF(xMid-50,yMid-50,xMid+50,yMid+50);
            if(r.contains(x,y)){
                return a;
            }
        }
        return null;
    }
}
