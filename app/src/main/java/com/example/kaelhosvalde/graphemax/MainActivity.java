package com.example.kaelhosvalde.graphemax;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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

public class MainActivity extends AppCompatActivity {

    private static Graph firstGraph;
    private ImageView imgv;
    private static DrawableGraph graphe;


    private float lastTouchDownX;
    private float lastTouchDownY;
    private AlertDialog alertDialog;
    private String etiquette,etiquetteNode,newNodeSize,etiquetteNewNode;
    private Boolean onNode = false, onArc = false;
    private Node activNode;
    private ArcFinal activArc;
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
        Point size = new Point();
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
        else if(onArc && modeModification){
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = this.getMenuInflater();
            inflater.inflate(R.menu.menu_context_arc, menu);
        }
        else if(modeModification) {
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
                            .setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
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
                            .setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
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
                            .setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
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

                return true;
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
                return true;
            case R.id.mnuMoovGraph:
                modeModification =false;
                modeCreationArc =false;
                modeDeplacementNoeuds = true;
                return true;
            case R.id.mnuUpdateNodeArc:
                modeModification =true;
                modeCreationArc =false;
                modeDeplacementNoeuds = false;
                return true;
            case R.id.mnuResetGraph:
                modeCreationArc =true;
                modeDeplacementNoeuds = false;
                modeModification = false;

                for(Node n:firstGraph.getNodes()){
                    firstGraph.removeNode(n);
                }

                firstGraph.initialisationGraph();
                updateView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
