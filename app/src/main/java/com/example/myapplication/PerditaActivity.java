package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PerditaActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static Context context;
    private UserSession userSession;
    private TextView TextViewGalattico;
    private TextView TextViewPerdita;
    private Button btnHome6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perdita);
        mAuth = FirebaseAuth.getInstance();
        userSession = UserSession.getInstance();
        context = getApplicationContext(); //abbiamo il contesto
        update();
    }

    public void onClickHome(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void update() {
        Partita p = userSession.getPartita(); //ci da la partita in corso
        if (p != null) {
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            p.dataEnd = dt.format(LocalDateTime.now()); //cambiamo data
            p.stato = 2; //cambiamo stato
            if (p.giocatori.size() > 1)
                p.idUserWinner = userSession.isAdmin() ? userSession.USER_UID :  p.idUser;

            Common.updatePartita(p);
        }
    }
}