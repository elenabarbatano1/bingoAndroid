package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static Context context;
    private UserSession userSession;
    private Button btnStart;
    private Button bottoneCreaPartita;
    private Button bottoneCercaPartita;
    private Button bottoneRegole;
    private Button btnDashboard;
    private TextView textViewTitolobingo;
    private ProgressBar pbAttesa;
    private Partita partitaInCorso;
    
    //private String Uid;
    //private GlobalConstant global= GlobalConstant.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Menu");
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        context = getApplicationContext();
        ProgressBar pbAttesa = findViewById(R.id.pbAttesa);
        // Initialize user session
        userSession = UserSession.getInstance();
        if (userSession.isLogged == false) {
            onClicksignInAnonymously(null);
        } else {
            Button btnStart = findViewById(R.id.btnStart);
            btnStart.setVisibility(View.INVISIBLE); //rende invisibile il pulsante LOGIN
        }

        Common.caricaPartiteInCorso(pbAttesa);

        // Listener creazione nuova partita!
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("Partite")
                .whereEqualTo("stato", 0)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    partitaInCorso = dc.getDocument().toObject(Partita.class);
                                    Log.d(TAG, "Nuova partita: " + dc.getDocument().getData());

                                    Common.caricaPartiteInCorso(pbAttesa);

                                    Toast.makeText(context, "E' stata aggiunta una nova partita!", Toast.LENGTH_SHORT).show();

                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Partita modificata: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Partita rimossa: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });
    }

    //metodo per il login dell'utente, non serve nome e password
    public void onClicksignInAnonymously(View view) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //MI RICAVO L'ID E LO SALVO IN UNA VARIABILE GLOBALE
                            //GlobalConstant.USER_UID = mAuth.getCurrentUser().getUid();
                            userSession.USER_UID = mAuth.getCurrentUser().getUid();
                            userSession.isLogged = true; //sappiamo di essere loggati
                            btnStart = findViewById(R.id.btnStart);
                            btnStart.setVisibility(View.INVISIBLE); //rende invisibile il pulsante LOGIN
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void onClickCercaPartita(View view) 
    {
        Partita p = userSession.getPartita(); // da la partita in corso
        if (p == null || userSession.partiteLista.size() == 0 || (p.stato == 2 || p.stato == -1)) {
            Toast.makeText(context, "Errore, nessuna partita trovata", Toast.LENGTH_LONG).show();
            Common.caricaPartiteInCorso(null);
            return;
        }

        // check numero massimo giocatori...
        if (p.giocatori.size() > GlobalConstant.MAX_GIOCATORI) {
            Toast.makeText(context, "Errore, numero massimo di giocatori superato. Attenti la conclusione della partita!", Toast.LENGTH_LONG).show();
            return;
        }

        // check partita già iniziata. Lo capisco se è stato estratto almeno un numero e che ci siano minimo due giocatori!
        if (p.giocatori.size() >= GlobalConstant.MIN_GIOCATORI && p.numeroEstratto > 0) {
            Toast.makeText(context, "Errore, la partita è già in corso. Attenti la conclusione della partita!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(context, GameActivity.class);
        startActivity(i);
    }

    public void onClickCreaPartita(View view) {
        Intent i = new Intent(context, CreateLobby.class);
        startActivity(i);
    }

    public void onClickDashboard(View view) {
        Intent i = new Intent(context, DashboardActivity.class);
        startActivity(i);
    }
    public void onClickComesigioca(View view) {
        Intent i = new Intent(context, RegoleActivity.class);
        startActivity(i);
    }

}