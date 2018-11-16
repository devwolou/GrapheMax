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
import android.util.Log;


public class DrawableGraph extends Drawable {

    private Graph graph;

    public DrawableGraph (Graph g){
        graph = g;
    }

    /**
     * Méthode par dichotomie pour trouver un point d'intersection entre l'arc et le bord d'un noeud
     * @param edgePath
     * @param a
     * @return
     */
    public Path findPoint(Path edgePath, ArcFinal a)
    {
        //creation des variables notamment les bornes supérieur et inférieure du path et la varible point contenant les coordonnee du point
        double ecart = 1e-5;
        float[] point = {0f, 0f};
        float borneInf =0, borneSup = 1 ,mid ;
        Region region;
        PathMeasure pm = new PathMeasure(edgePath,false);
        //Tant que la borne superieure reste superieure a la borne inferieure, on continue la dichotomie
        while(borneInf < borneSup - ecart) {
            Path path = new Path();
            RectF rectF = new RectF(a.getNodeTo());
            path.addRoundRect(rectF, 40, 40, Path.Direction.CW);
            path.computeBounds(rectF, true);
            region = new Region();
            region.setPath(path, new Region((int)rectF.left, (int) rectF.top, (int)rectF.right, (int) rectF.bottom));
            mid = (borneInf + borneSup) / 2;
            pm.getPosTan(pm.getLength() * mid, point, null);
            //Si le point est contenu dans la region, alors la borne supérieure devient le milieu, sinon c'est la borne inferieure qui devient le milieu
            if(!region.contains((int)point[0], (int)point[1])) {
                borneInf = mid;
            }
            else {
                borneSup = mid;
            }
        }
        //point temporarire afin de calculer deux autres points
        float[] pointTemp = {0f, 0f};
        int widthFleche = a.getWidth() * 3;

        pm.getPosTan(pm.getLength() * (borneSup - (widthFleche/pm.getLength())), pointTemp, null);

        //On calcule deux points afin de dessiner la fleche
        float[] pointA = {pointTemp[0] + point[1] - pointTemp[1], pointTemp[1] + pointTemp[0] - point[0]};
        float[] pointB = {pointTemp[0] + pointTemp[1] - point[1], pointTemp[1] + point[0] - pointTemp[0]};
        //Cree un path créant la fleche a partir des points

        Path pfleche = new Path();
        pfleche.moveTo(point[0], point[1]);
        pfleche.lineTo(pointA[0], pointA[1]);
        pfleche.moveTo(point[0], point[1]);
        pfleche.lineTo(pointB[0], pointB[1]);

        return pfleche;
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
                if (a.hasBeenModifiedABoucle) {
                    midPoint = a.getMidPoint();
                    path.cubicTo(
                            midPoint[0] + 5*n.getRayon(), midPoint[1] + 5*n.getRayon(),
                            midPoint[0] - 5*n.getRayon(), midPoint[1] + 5*n.getRayon(),
     //                       n.centerX() - 5*n.getRayon(), n.centerY() + 5*n.getRayon(),
                            n.centerX(), n.centerY()
                    );

                } else {
                    // Dessiner boucle
                    path.cubicTo(
                            n.centerX() + 5*n.getRayon(), n.centerY() + 5*n.getRayon(),
                            n.centerX() - 5*n.getRayon(), n.centerY() + 5*n.getRayon(),
                            n.centerX(), n.centerY()
                    );
                    PathMeasure pm = new PathMeasure(path, false);
                    pm.getPosTan(pm.getLength() / 2, midPoint, tangent);
                    a.setMidPoint(midPoint);
                    a.setTangent(tangent);
                }

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


            //On cree un paint pour la fleche avec certains attributs égaux à ceux de l'arc
            Path pathFleche = findPoint(path,a);
            Paint paintFleche = new Paint();
            paintFleche.setColor(a.getColor());
            paintFleche.setStrokeWidth(a.getWidth());
            paintFleche.setStyle(Paint.Style.STROKE);

            canvas.drawPath(pathFleche,paintFleche);
            canvas.drawPath(path, pArc);


            //Création de l'étiquette
            Paint pTexte = new Paint();
            pTexte.setColor(Color.BLACK);
            pTexte.setTextSize(a.getLargeurEtiquette());
            pTexte.setTextAlign(Paint.Align.CENTER);
            if(a.getLargeurEtiquette()<=30) {
                canvas.drawText(a.getEtiquette(), midPoint[0] -(a.getLargeurEtiquette()/2), midPoint[1] + 20, pTexte);
            } else{
                canvas.drawText(a.getEtiquette(), midPoint[0] -(a.getLargeurEtiquette()/2), midPoint[1] + (20+(a.getLargeurEtiquette()-30)), pTexte);
            }
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
