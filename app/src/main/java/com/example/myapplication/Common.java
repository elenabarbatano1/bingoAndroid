package com.example.myapplication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Common {
    //riporta tutti metodi riutilizzabili
    public static String getUid() {
        return UUID.randomUUID().toString(); //codice univoco
    }

    public static void caricaPartiteInCorso(ProgressBar pbAttesa) { //lo richiamo nella onCreate della mainActivity
        //LETTURA
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbPartite = database.collection("Partite");

        //0=avviata, 1=in corso(appena entra un giocatore)
        dbPartite.whereIn("stato", Arrays.asList(0, 1)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> partiteLista = queryDocumentSnapshots.getDocuments();
                List<Partita> listaP = new ArrayList<Partita>();
                for (DocumentSnapshot d : partiteLista) { //itero sui documenti
                    Partita p1 = d.toObject(Partita.class);
                    p1.idPartita = d.getId(); //ci prendiamo l'id della partita

                    listaP.add(p1);
                }
                if (pbAttesa != null) {
                    pbAttesa.setVisibility(View.INVISIBLE);
                }
                UserSession.getInstance().partiteLista = listaP; //ci salviamo la lista in modo da non richiare firebase (richiamo la classe e il metodo usersession)
            }
        });
    }

    /*
    public static void caricaPartiteByStato(int stato, ProgressBar pbAttesa) {
        //LETTURA
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbPartite = database.collection("Partite");

        dbPartite.whereEqualTo("stato", stato).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> partiteLista = queryDocumentSnapshots.getDocuments();
                List<Partita> listaP = new ArrayList<Partita>();
                for (DocumentSnapshot d : partiteLista) { //itero sui documenti
                    Partita p1 = d.toObject(Partita.class);
                    p1.idPartita = d.getId(); //ci prendiamo l'id della partita

                    listaP.add(p1);
                }
                if (pbAttesa != null) {
                    pbAttesa.setVisibility(View.INVISIBLE);
                }
                UserSession.getInstance().partiteLista = listaP; //ci salviamo la lista in modo da non richiare firebase (richiamo la classe e il metodo usersession)
            }
        });
    }
    */

    public static int generateRandom(int min, int max) {
        int val = (int) Math.floor(Math.random() * (max - min + 1) + min);
       /* if(UserSession.getInstance().numeriChiamatiLista.contains(val)){ //controllo se val è già presente se è presente richiamata il metodo
          return generateRandom(min, max);
        }*/
        while (UserSession.getInstance().numeriChiamatiLista.contains(val)) {
            val = (int) Math.floor(Math.random() * (max - min + 1) + min);
        }
        return val;
    }

    public static int generateRandomCartella(int min, int max) {
        int val = (int) Math.floor(Math.random() * (max - min + 1) + min);
        /*if(UserSession.getInstance().numeriCartellaLista.contains(val)){ //controllo se val è già presente se è presente richiamata il metodo
            return generateRandom(min, max);
        }*/
        while (UserSession.getInstance().numeriCartellaLista.contains(val)) {
            val = (int) Math.floor(Math.random() * (max - min + 1) + min);
        }
        return val;
    }

    public static void updatePartita(Partita p) {
        if (p != null) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            CollectionReference dbPartite = database.collection("Partite");

            dbPartite.document(p.idPartita).set(p);
            //userSession.partiteLista.remove(p);//svuota la partita in corso
        }
    }
}
