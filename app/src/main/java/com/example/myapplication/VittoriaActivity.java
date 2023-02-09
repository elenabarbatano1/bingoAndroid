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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VittoriaActivity extends AppCompatActivity {
    private static Context context;
    private UserSession userSession;
    private TextView TextViewGalattico;
    private TextView TextViewVittoria;
    private Button btnHome5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vittoria);
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
        if(p != null) {

            DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            p.dataEnd = dt.format(LocalDateTime.now()); //cambiamo data
            p.stato = 2; //cambiamo stato
            p.idUserWinner = userSession.USER_UID;

            Common.updatePartita(p);
        }else{
            finish();
            Toast t = Toast.makeText(context, "Errore, non gestito", Toast.LENGTH_SHORT);
            t.show();
        }
    }
}