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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

        Common.caricaPartite(0, pbAttesa);
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

    public void onClickCercaPartita(View view) {
        if (userSession.partiteLista.size() > 0 && userSession.getPartita().stato != 2 && userSession.getPartita().stato != -1) {
            Intent i = new Intent(context, GameActivity.class);
            startActivity(i);
            //Toast t = Toast.makeText(context, "La partita è stata trovata", Toast.LENGTH_SHORT);
            //t.show();
        } else {
            Toast t = Toast.makeText(context, "Errore, nessuna partita trovata", Toast.LENGTH_SHORT);
            t.show();
            Common.caricaPartite(0, null);
        }
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