package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CreateLobby extends AppCompatActivity {
    private static Context context;
    private boolean partitaInCorso = false;
    private UserSession userSession;
    private Button btnHome2;
    private Button btnCreaPartita;
    private TextView textViewNomePartita;
    private TextView textViewTitolo;
    private EditText editTextNomePartita;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Create Lobby");
        setContentView(R.layout.activity_createlobby);
        context = getApplicationContext();
        // Initialize user session
        userSession = UserSession.getInstance();

        //controllo partite in corso
        if (userSession.partiteLista.size() > 0) {
            Toast t = Toast.makeText(getApplicationContext(), "Errore, esiste già una partita in corso!", Toast.LENGTH_SHORT);
            t.show();
            finish();
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    public void onClickCreaPartita(View view) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbPartite = database.collection("Partite");
        Log.d("onClick", "prova");
        Partita p = new Partita();

        //come leggere da input
        editTextNomePartita = findViewById(R.id.editTextNomePartita);
        p.nomePartita = editTextNomePartita.getText().toString();
        if (p.nomePartita.length() == 0) {
            Toast t = Toast.makeText(getApplicationContext(), "Errore, nome partita non valido!", Toast.LENGTH_SHORT);
            t.show();
            return;
        }
        p.stato = 0; //stiamo avviando la partita
        p.numeroEstratto = 0;
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        p.dataStart = dt.format(LocalDateTime.now());
        p.idUser = userSession.USER_UID; //id di chi crea la partita
        p.username = "Miky";
        p.giocatori.add(p.idUser); //mi salvo i giocatori
        userSession.addPartita(p); //gli settiamo la partita in corso


        //dobbiamo far vedere il caricamento loader...... e disabilitiamo bottone crea partita
        dbPartite.add(p).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(Task<DocumentReference> task) {
                Common.caricaPartiteInCorso(null);
                Toast.makeText(getApplicationContext(), "Partita creata con successo: " + userSession.USER_UID, Toast.LENGTH_SHORT).show();
                //dobbiamo nascondere loader......
                finish();
            }
        });
    }

    public void onClickHome(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }
}