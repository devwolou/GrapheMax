package com.example.kaelhosvalde.graphemax;

import java.io.Serializable;

public class  ArcBoucle extends ArcFinal implements Serializable {

    public ArcBoucle(Node n,String etiquette) {
        super(n, n, etiquette);
    }
}
