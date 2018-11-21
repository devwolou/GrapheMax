package com.example.kaelhosvalde.graphemax;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public abstract class Serialiser {
    /**
     * Serialisation d'un objet
     * @param filename
     * @param objetserliser
     */
    public static boolean serialise(String filename,Object objetserliser,Context context){

        /*String location =Environment.getExternalStorageDirectory().toString()+File.separator+"GrapheCW"+File.separator;
        File directory = new File(location);
        if(!directory.exists())
            directory.mkdir();*/
        //FileOutputStream file = null;
        //File sdCard = Environment.getExternalStorageDirectory();
        //File filetest = new File(sdCard,filename);
        try {

            //file = new FileOutputStream(filetest);
            FileOutputStream file = context.openFileOutput(filename,Context.MODE_PRIVATE);
            ObjectOutputStream objOS;
            try {
                objOS = new ObjectOutputStream(file);
                objOS.writeObject(objetserliser);
                objOS.flush();
                objOS.close();
                return true;

            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Déserialisation d'un objet
     * @param filename
     * @param context
     * @return
     */
    public static Object deserialise(String filename, Context context){
        try{
            FileInputStream file = context.openFileInput(filename);
            ObjectInputStream objIS;
            try{
                objIS = new ObjectInputStream(file);
                try{
                    Object objet = objIS.readObject();
                    objIS.close();
                    return  objet;

                }catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }catch (StreamCorruptedException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

        }catch (FileNotFoundException e){
            //fichier non trouvé
            e.printStackTrace();
        }
        return null;

    }
}
