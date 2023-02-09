package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class activity_attesa extends AppCompatActivity {
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attesa);
        context = getApplicationContext();
    }

    public void onClickAnnulla(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
        finish();
    }
}