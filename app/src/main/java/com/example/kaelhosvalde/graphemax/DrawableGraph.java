package com.example.kaelhosvalde.graphemax;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;



public class DrawableGraph extends Drawable {

    private Graph graph;

    public DrawableGraph (Graph g){
        graph = g;
    }

    public void draw(Canvas canvas) {

        Paint pArcTemp = new Paint();
        pArcTemp.setStrokeWidth(5);
        pArcTemp.setColor(Color.BLACK);
        pArcTemp.setStyle(Paint.Style.STROKE);
        Path path;
        Path pathTemp;

        //On dessine d'abord les arcs
        for(ArcFinal a : graph.getArcs()){
            Paint pArc = new Paint();
            pArc.setStrokeWidth(a.getWidth());
            pArc.setColor(a.getColor());
            pArc.setStyle(Paint.Style.STROKE);
            float [] midPoint = {0f, 0f};
            float [] tangent = {0f, 0f};
            path = new Path();
            pathTemp = new Path();
            pathTemp.moveTo(a.getNodeFrom().centerX(), a.getNodeFrom().centerY());
            path.moveTo(a.getNodeFrom().centerX(), a.getNodeFrom().centerY());

            if (a instanceof ArcBoucle) {
                Node n = a.getNodeFrom();
                // Dessiner boucle
                path.cubicTo(n.centerX()+n.getRayon()+60,n.centerY()+n.getRayon()+40,
                        n.centerX()+n.getRayon()+60,n.centerY()-n.getRayon()-40,
                        n.centerX(),n.centerY());
                PathMeasure pm = new PathMeasure(path,false);
                pm.getPosTan(pm.getLength()/2,midPoint,tangent);
                a.setMidPoint(midPoint);
                a.setTangent(tangent);

            } else {

                //Si la courbe a deja été modifiée, on reprend le milieu qui convient
                if(a.hasBeenModified){
                    midPoint = a.getMidPoint();
                    path.quadTo(midPoint[0],midPoint[1], a.getNodeTo().centerX(), a.getNodeTo().centerY());
                } else {
                    pathTemp.lineTo(a.getNodeTo().centerX(), a.getNodeTo().centerY());
                    PathMeasure pm = new PathMeasure(pathTemp,false);
                    pm.getPosTan(pm.getLength()/2,midPoint,tangent);
                    path.quadTo(midPoint[0],midPoint[1], a.getNodeTo().centerX(), a.getNodeTo().centerY());
                    a.setMidPoint(midPoint);
                    a.setTangent(tangent);
                }

            }


            canvas.drawPath(path, pArc);
            //Création de l'étiquette
            Paint pTexte = new Paint();
            pTexte.setColor(Color.BLACK);
            pTexte.setTextSize(a.getLargeurEtiquette());
            pTexte.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(a.getEtiquette(), midPoint[0] + 20, midPoint[1] + 20, pTexte);

        }

        //On dessine un arc temporaire si il est en cours de création
        ArcTemporaire tempArc = graph.getArcTemp();
        if (tempArc != null) {
            path = new Path();
            path.moveTo(tempArc.getNodeFrom().centerX(), tempArc.getNodeFrom().centerY());
            path.lineTo(tempArc.getNodeX(), tempArc.getNodeY());
            canvas.drawPath(path, pArcTemp);
        }


        Paint p = new Paint();
        Paint pTexte = new Paint();
        pTexte.setColor(Color.WHITE);
        pTexte.setTextSize(30);
        pTexte.setTextAlign(Paint.Align.CENTER);

        for (Node n : graph.getNodes()) {
            float tailleTexte = pTexte.measureText(n.getEtiquette()) / 2;
            if (n.getRayonDefault() < tailleTexte) {
                n.setRayonDefault(tailleTexte + 10);
            }

            p.setColor(n.getColor());
            canvas.drawRoundRect(n, 40, 40, p);
            canvas.drawText(n.getEtiquette(), n.centerX(), n.centerY(), pTexte);
        }

    }
        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
}
