package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegoleActivity extends AppCompatActivity {
    private static Context context;
    private Button btnHome4;
    private TextView twTitolo;
    private TextView twRegole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Regole");
        setContentView(R.layout.activity_regole);
        context = getApplicationContext(); //abbiamo il contesto
    }
    public void onClickHome(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }
}