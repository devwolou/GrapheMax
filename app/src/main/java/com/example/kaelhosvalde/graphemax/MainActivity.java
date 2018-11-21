package com.example.kaelhosvalde.graphemax;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static Graph firstGraph;
    private ImageView imgv;
    private static DrawableGraph graphe;


    private float lastTouchDownX;
    private float lastTouchDownY;
    private AlertDialog alertDialog;
    private String etiquette,etiquetteNode,newNodeSize,etiquetteNewNode, etiquetteArc,epaisseurArc,NewSizeEtiquette;
    private Boolean onNode = false, onArc = false;
    private Node activNode;
    private ArcFinal activArc;
    public Point size;
//max=========================================================
    private ArcFinal activArcBoucle;



    private String value;
    private boolean modeCreationArc = true, modeDeplacementNoeuds = false, modeModification = false, modeCourbure = false;

    //@SuppressLint("ClickableViewAccessibility")
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Recuperation de la taille de l'écran
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        //initialisation du graph et d'image view
        this.registerForContextMenu(this.findViewById(R.id.imgView));
        if (firstGraph == null) {
            firstGraph = new Graph(size);
        }
        imgv = (ImageView) findViewById(R.id.imgView);

        if (graphe == null) {
            graphe = new DrawableGraph(firstGraph);
        }
        imgv.setImageDrawable(graphe);

        imgv.setOnTouchListener(new View.OnTouchListener() {
            boolean wasOnNode = false;
            Float xBeginArc, yBeginArc;
            Node arcBeginNode;
            ArcFinal arc;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //on recupere les coordonnées
                lastTouchDownX = event.getX();
                lastTouchDownY = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isOnNode()) {
                            arcBeginNode = activNode;
                            wasOnNode = true;
                            xBeginArc = lastTouchDownX;
                            yBeginArc = lastTouchDownY;

                            //on initialise l'arc temporaire pour la création de l'arc
                            if (modeCreationArc) {
                                firstGraph.initArcTemp(lastTouchDownX, lastTouchDownY);
                            }
                            updateView();
                        } else {
                            activNode = null;
                            activArc = null;
                            arc = null;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Mode CREATION d'ARC
                        if (modeCreationArc) {
                            //Si on étais sur un noeud et que l'on arrive sur un noeud (fin creation arc)
                            if (wasOnNode && isOnNode()) {

                                //affichage dialogue pour l'étiquette de l'arc
                                final EditText input = new EditText(MainActivity.this);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertDialogBuilder.setTitle(R.string.AjoutArc);

                                // set dialog message
                                alertDialogBuilder
                                        .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // if this button is clicked, close
                                                // current activity
                                                etiquette = input.getText().toString();

                                                if (etiquette.length() > 0) {

                                                    //Si l'arc de début = l'arc de fin, on créer un objet boucle
                                                    if (arcBeginNode == activNode) {
                                                        firstGraph.addArc(new ArcBoucle(activNode, etiquette));
                                                        updateView();

                                                        //sinon on creer un objet arc simple
                                                    } else {
                                                        firstGraph.addArc(new ArcFinal(arcBeginNode, activNode, etiquette));
                                                        updateView();
                                                    }
                                                    input.setText("");
                                                    arcBeginNode = null;
                                                    wasOnNode = false;
                                                }

                                            }
                                        });

                                alertDialogBuilder.setView(input);
                                // create alert dialog
                                alertDialog = alertDialogBuilder.create();
                                // show it
                                alertDialog.show();

                            } else {
                                arcBeginNode = null;
                                wasOnNode = false;
                            }

                            //on detruit l'arc temporaire
                            firstGraph.makeArcTempNull();
                            updateView();
                            //Si on veut déplacer un noeud, on change son centre avec les dernieres coordonnées
                        } else if (isOnNode()) {
                            activNode.setCenter(lastTouchDownX, lastTouchDownY);
                            onNode = false;
                            updateView();

                            //Fin de la courbure de l'arc, on calcule une derniere fois le milieu
                        }else {
                            arcBeginNode = null;
                        }updateView();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        //On créer un arc temporaire en suivant le doigt


                        if (modeCreationArc) {
                            if (firstGraph.getArcTemp() != null) {
                                firstGraph.setArcTemp(lastTouchDownX, lastTouchDownY);
                                updateView();
                            }

                            //On change le placement du noeud
                        } else if (modeDeplacementNoeuds && isOnNode()) {
                            activNode.setCenter(lastTouchDownX, lastTouchDownY);
                            updateView();

//Max=============================================
                            //On change la courbure de l'arc
                        } else if(modeCourbure && isOnArc() && !isOnArcBoucle()){
                            arc = activArc;
                            Log.i("je rentre: ","moi");
                            calculerMilieuArc();
                        }
//Max======================================================================================04/11
                        else if (isOnArcBoucle() && modeCourbure){
                            Log.i("je rentre: ","moicourbe");
                            calculerMilieuArc();
                            updateView();
                        }
                        break;
                }
                return false;
            }
        });


        imgv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText input = new EditText(MainActivity.this);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

                //Si on est sur un noeud, on affiche un menu contextuel
                for(Node n :  firstGraph.getNodes())
                {
                    if(n.contains(lastTouchDownX,lastTouchDownY)) {
                        onNode = true;
                        activNode = n;
                        return false;
                    }
                }


                if (modeModification){
                    onArc = isOnArc();
                    return false;
                }
                return true;
            }
        });

    }

    /**
     * Ajout du menu principal
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_activity_main,menu);
        return true;
    }

    /**
     *Ajout du menu contextuel sur l'ImageView
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(onNode && modeModification) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = this.getMenuInflater();
            inflater.inflate(R.menu.menu_context_node, menu);
        }
        else if(onArc && modeModification && !modeCourbure){
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = this.getMenuInflater();
            inflater.inflate(R.menu.menu_context_arc, menu);
        }
        if(modeModification && !onNode && !onArc && !modeCourbure) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = this.getMenuInflater();
            inflater.inflate(R.menu.menu_context_imgview, menu);
        }


        onNode=false;
        onArc = false;
    }

    /**
     * Méthode qui permet de savoir s'il on est sur un noeud ou non,
     * dans le cas où on est sur un noeud, affecte ce noeud à activNode
     * @return
     */
    public boolean isOnNode(){
        activNode = firstGraph.getOneNode(lastTouchDownX,lastTouchDownY);
        return activNode != null;
    }

//max=======================================================================
    public boolean isOnArcBoucle(){
        activArcBoucle = firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);

        return ((activArcBoucle != null)&&(activArcBoucle instanceof ArcBoucle));
    }




    /**
     * Méthode permettant de savoir s'il on est sur un arc ou non,
     * dans le cas où on est sur un arc, affecte cet arc à activArc
     * @return
     */
    public boolean isOnArc(){
        activArc = firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);
        return activArc != null;
    }

    private void updateView (){
        graphe = new DrawableGraph(firstGraph);
        imgv.setImageDrawable(graphe);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//Menu Modification Node
            case R.id.actionDeleteNode:
                if (isOnNode()){
                    Node n =firstGraph.getOneNode(lastTouchDownX,lastTouchDownY);
                    firstGraph.removeNode(n);
                    updateView();
                }
                break;

            case R.id.actionModifNodeEtiq:
                if(isOnNode() && modeModification){

                    final EditText inputEtiqNode = new EditText(MainActivity.this);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.etiquettenoeud);

                    // set dialog message
                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    etiquetteNode = inputEtiqNode.getText().toString();

                                    Node n =firstGraph.getOneNode(lastTouchDownX,lastTouchDownY);
                                    n.setEtiquette(etiquetteNode);
                                    updateView();
                                    }
                            });

                    alertDialogBuilder.setView(inputEtiqNode);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                break;

            case R.id.actionResizeNode:
                if(isOnNode() && modeModification){

                    final EditText inputResizeNode = new EditText(MainActivity.this);
                    inputResizeNode.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.tailleNode);

                    // set dialog message

                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    newNodeSize = inputResizeNode.getText().toString();

                                    Node n =firstGraph.getOneNode(lastTouchDownX,lastTouchDownY);
                                    n.resizeNode(Float.valueOf(newNodeSize));
                                    updateView();
                                }
                            });

                    alertDialogBuilder.setView(inputResizeNode);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                break;

            case R.id.actionModifNodeColor:

                if(isOnNode() && modeModification){

                    final Spinner inputColorNode = new Spinner(this);
                    ArrayAdapter<CharSequence> adapterNode = ArrayAdapter.createFromResource(this, R.array.color_arrays, android.R.layout.simple_spinner_item);
                    adapterNode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    inputColorNode.setAdapter(adapterNode);
                    AlertDialog.Builder alertDialogBuilderColorNode = new AlertDialog.Builder(
                            this);
                    // set title
                    alertDialogBuilderColorNode.setTitle(R.string.changerCouleur);

                    // set dialog message
                    alertDialogBuilderColorNode
                            .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //if this button is clicked, close
                                    // current activity

                                    int pos = inputColorNode.getSelectedItemPosition();
                                    switch (pos){
                                        case 0:
                                            activNode.setColor(Color.RED);
                                            updateView();
                                            break;
                                        case 1:
                                            activNode.setColor(Color.GREEN);
                                            updateView();
                                            break;
                                        case 2:
                                            activNode.setColor(Color.BLUE);
                                            updateView();
                                            break;
                                        case 3:
                                            activNode.setColor(Color.parseColor("#f49542"));
                                            updateView();
                                            break;
                                        case 4:
                                            activNode.setColor(Color.CYAN);
                                            updateView();
                                            break;
                                        case 5:
                                            activNode.setColor(Color.MAGENTA);
                                            updateView();
                                            break;
                                        case 6:
                                            activNode.setColor(Color.BLACK);
                                            updateView();
                                            break;
                                    }

                                }
                            });

                    alertDialogBuilderColorNode.setView(inputColorNode);
                    // create alert dialog
                    alertDialog = alertDialogBuilderColorNode.create();
                    alertDialog.show();
                }
                break;

            case R.id.actionAddNode:

                //On affiche un dialogue permettant de remplir l'étiquette du noeud en mode création de noeud
                if (modeModification && !isOnNode()) {

                    final EditText inputAddNode = new EditText(MainActivity.this);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    // set title
                    alertDialogBuilder.setTitle(R.string.creerNoeud);

                    // set dialog message
                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    etiquetteNewNode = inputAddNode.getText().toString();

                                    Node node = new Node(lastTouchDownX, lastTouchDownY, etiquetteNewNode, Color.BLACK);
                                    if (etiquetteNewNode.length() > 0) {
                                        firstGraph.addNode(node);
                                        updateView();
                                        inputAddNode.setText("");
                                    }

                                }
                            });

                    alertDialogBuilder.setView(inputAddNode);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }

//Menu Modification Arc=================================================================================
            //Supprimer un Arc
            case R.id.actionDeleteArc:
                if (isOnArc()){
                    ArcFinal a = firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);
                    firstGraph.removeArc(a);
                    updateView();
                }
                break;

            //Renommer l'etiquette d'un arc
            case R.id.actionModifEtqArc:
                if(isOnArc() && modeModification){

                    final EditText inputNewEtiqArc = new EditText(MainActivity.this);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.etiquetteArc);

                    // set dialog message
                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    etiquetteArc = inputNewEtiqArc.getText().toString();

                                    ArcFinal a =firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);
                                    a.setEtiquette(etiquetteArc);
                                    updateView();
                                }
                            });

                    alertDialogBuilder.setView(inputNewEtiqArc);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                break;

            //Modifier la couleur d'un Arc
            case R.id.actionModifArcColor:

                if(isOnArc() && modeModification){

                    final Spinner inputColorArc = new Spinner(this);
                    ArrayAdapter<CharSequence> adapterNode = ArrayAdapter.createFromResource(this, R.array.color_arrays, android.R.layout.simple_spinner_item);
                    adapterNode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    inputColorArc.setAdapter(adapterNode);
                    AlertDialog.Builder alertDialogBuilderColorNode = new AlertDialog.Builder(
                            this);
                    // set title
                    alertDialogBuilderColorNode.setTitle(R.string.changerCouleur);

                    // set dialog message
                    alertDialogBuilderColorNode
                            .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //if this button is clicked, close
                                    // current activity

                                    int pos = inputColorArc.getSelectedItemPosition();
                                    switch (pos){
                                        case 0:
                                            activArc.setColor(Color.RED);
                                            updateView();
                                            break;
                                        case 1:
                                            activArc.setColor(Color.GREEN);
                                            updateView();
                                            break;
                                        case 2:
                                            activArc.setColor(Color.BLUE);
                                            updateView();
                                            break;
                                        case 3:
                                            activArc.setColor(Color.parseColor("#f49542"));
                                            updateView();
                                            break;
                                        case 4:
                                            activArc.setColor(Color.CYAN);
                                            updateView();
                                            break;
                                        case 5:
                                            activArc.setColor(Color.MAGENTA);
                                            updateView();
                                            break;
                                        case 6:
                                            activArc.setColor(Color.BLACK);
                                            updateView();
                                            break;
                                    }

                                }
                            });

                    alertDialogBuilderColorNode.setView(inputColorArc);
                    // create alert dialog
                    alertDialog = alertDialogBuilderColorNode.create();
                    alertDialog.show();
                }
                break;

            //Modifier l'épaisseur d'un arc
            case R.id.actionResizeArc:
                if (isOnArc() && modeModification){
                    final EditText inputNewEpaisseurArc = new EditText(MainActivity.this);
                    inputNewEpaisseurArc.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.actionResizeArc);

                    // set dialog message
                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    epaisseurArc = inputNewEpaisseurArc.getText().toString();

                                    ArcFinal a =firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);
                                    a.setWidth(Integer.valueOf(epaisseurArc));
                                    updateView();
                                }
                            });

                    alertDialogBuilder.setView(inputNewEpaisseurArc);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                break;

            //Modifier l'épaisseur d'un arc
            case R.id.actionResizeEtiquette:
                if (isOnArc() && modeModification){
                    final EditText inputNewSizeEtiquette = new EditText(MainActivity.this);
                    inputNewSizeEtiquette.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.actionResizeEtiquette);

                    // set dialog message
                    alertDialogBuilder
                            .setPositiveButton(R.string.ajouter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    NewSizeEtiquette = inputNewSizeEtiquette.getText().toString();

                                    ArcFinal a =firstGraph.getOneArc(lastTouchDownX,lastTouchDownY);
                                    a.setLargeurEtiquette(Integer.valueOf(NewSizeEtiquette));
                                    updateView();
                                }
                            });

                    alertDialogBuilder.setView(inputNewSizeEtiquette);
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                break;
//                return true;
        }


        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuAddArc :
                modeCreationArc =true;
                modeDeplacementNoeuds = false;
                modeModification = false;
                modeCourbure=false;
                return true;
            case R.id.mnuMoovGraph:
                modeModification =false;
                modeCreationArc =false;
                modeDeplacementNoeuds = true;
                return true;
            case R.id.mnuUpdateNodeArc:
                modeModification =true;
                //max
                modeCourbure=false;
                modeCreationArc =false;
                modeDeplacementNoeuds = false;
                return true;
            case R.id.mnuResetGraph:
                modeCreationArc =true;
                modeDeplacementNoeuds = false;
                modeModification = false;
                modeCourbure=false;

                for(Node n:firstGraph.getNodes()){
                    firstGraph.removeNode(n);
                }

                firstGraph.initialisationGraph(size);
                updateView();
                return true;
            case R.id.mnuMoovArc:
                modeCourbure=true;
                modeDeplacementNoeuds = false;
                modeModification = false;
                modeCreationArc =false;
                return true;

            case R.id.sendMail:
                makeScreenShot(imgv);
                modeCourbure=false;
                modeDeplacementNoeuds = false;
                modeModification = false;
                modeCreationArc =true;
                return true;
            case R.id.mnuLoadSavedGraph:
                //Intent i = new Intent(MainActivity.this, GraphLoadActivity.class);
                //startActivity(i);
                Graph graphede = (Graph) Serialiser.deserialise("grapheSave.txt",MainActivity.this);
                firstGraph = null;
                firstGraph = graphede;
                updateView();
                return true;
            case R.id.mnuSaveGraph:
                if (Serialiser.serialise("grapheSave.txt", firstGraph.getNodes(), MainActivity.this)) {
                    Log.i("serial", "OK");

                } else Log.i("serial", "NON");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Méthode permettant de modifier la courbure de l'arc en prennant en compte la tangente
     * et le point de milieu des deux noeuds (en ligne droite)
     *
     * Cette méthode a été réalisé par Goaillo github (à revoir)===============================================
     */
//max===============================================================
    public void calculerMilieuArc(){
        Log.i("je rentre: ","je suis là");
        Node nFrom = activArc.getNodeFrom(), nTo = activArc.getNodeTo();
        Log.i("DEBUGNODE FROM",activArc.getNodeFrom().getEtiquette());
        Log.i("DEBUGNODE TO",activArc.getNodeTo().getEtiquette());

        if (activArc.getNodeFrom() == activArc.getNodeTo())
        {
            Log.i("EGALE","OK");
        }
        else
            Log.i("EGALE","NON");

        Path pathTemp = new Path();
        pathTemp.moveTo(nFrom.centerX(),nFrom.centerY());
        if(activArc.getNodeFrom() == activArc.getNodeTo()){
            pathTemp.cubicTo(nFrom.centerX() + nFrom.getRayon() + 60, nFrom.centerY() + nFrom.getRayon() + 40,
                    nFrom.centerX() + nFrom.getRayon() + 60, nFrom.centerY() - nFrom.getRayon() - 40,
                    nFrom.centerX(), nFrom.centerY());
        }else {

            pathTemp.quadTo((nFrom.centerX() + nTo.centerX()) / 2, (nFrom.centerY() + nTo.centerY()) / 2, nTo.centerX(), nTo.centerY());
        }
        ////La partie de calcul sur les coefficients des différentes droites à été réalisée à l'aide de Kévin Boisgontier

        float[] mid = {0, 0}, tan = {0, 0};
        PathMeasure pm = new PathMeasure(pathTemp,false);
        pm.getPosTan(pm.getLength()/2, mid, tan);

        //Dernieres coordonnées touchées
        float x1 = lastTouchDownX;
        float y1 = lastTouchDownY;

        //Coefficient pour le calcul du projeté orthogonal
        float c = (tan[0]*(mid[0]-x1)+tan[1]*(mid[1]-y1))/(tan[0]*tan[0]+tan[1]*tan[1]);

        //Coefficients de la droite parallèle à la tangente passant par x1 et y1
        float m1 = tan[1]/tan[0];
        float b1 = y1-m1*x1;

        //Coefficients de la droite perpendiculaire à la tangente
        float m2 = (y1-mid[1]+tan[1]*c)/(x1-mid[0]+tan[0]*c);
        float b2 = mid[1]-m2*mid[0];

        //Point d'intersection des deux droites
        float x = (b2-b1)/(m1-m2);
        float y = m1*((b2-b1)/(m1-m2))+b1;


        if(tan[0]==0){
            x = x1;
            y = mid[1];
        }
        if(tan[1]==0){
            x = mid[0];
            y = y1;
        }

        float[] newMid = {x,y};
        activArc.setMidPointCourb(newMid);
        updateView();
    }

    public void makeScreenShot(View view){
        View v1 = view.getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bm = v1.getDrawingCache();

        StoreByteImage(bm,100);
    }

    public boolean StoreByteImage(Bitmap myImage, int quality)
    {
        FileOutputStream fileOutputStream = null;

        File sdCard = Environment.getExternalStorageDirectory();
        String filename= "GrapheCW" + 1 + ".jpg";
        File file = new File(sdCard, filename);
        try
        {
            fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            myImage.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
        }
        catch (FileNotFoundException e)
        {
            Log.i("EXCP FNF", e.getMessage());
        }
        catch (IOException e)
        {
            Log.i("EXCP IO", e.getMessage());
        }
        envoieMail(filename);

        return true;
    }

    public void envoieMail(String file){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        String[] recipients = new String[]{"", "",};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, file);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Bonne réception");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+file));
        emailIntent.setType("text/plain");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        finish();

    }
}
