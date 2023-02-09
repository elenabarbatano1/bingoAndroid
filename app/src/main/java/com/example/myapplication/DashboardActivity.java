package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private static Context context;
    private UserSession userSession;
    public List<Partita> storicoPartiteLista; //mi salvo tutte le partite
    private Button btnHome3;
    private Button btnclear;
    private ProgressBar pbAttesa;
    private TableLayout tableDashboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Dashboard");
        setContentView(R.layout.activity_dashboard);
        context = getApplicationContext(); //abbiamo il contesto
        ProgressBar pbAttesa = findViewById(R.id.pbAttesa);

        // Initialize user session
        userSession = UserSession.getInstance();
        storicoPartiteLista =new ArrayList<Partita>();
       /* if(userSession.getPartita() == null){ //richiama il carica partite
            caricaPartite(2, pbAttesa);
        }else{
            pbAttesa.setVisibility(View.INVISIBLE);
        }*/

        //initTable();
        caricaPartite(2, pbAttesa);
    }

    public void caricaPartite(int stato, ProgressBar pbAttesa) {
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
                storicoPartiteLista = listaP; //mi salvo tutte le partite
                initTable();
            }
        });
    }

    public void onClickHome(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }

    public void initTable() {
        tableDashboard = findViewById(R.id.tableDashboard);
        tableDashboard.removeAllViews();
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(20, 20, 20, 20);

        tableRowParams.weight = 1;

        if (tableDashboard != null) {
            //creo le colonne
            TableRow rigaIntestazione = new TableRow(context);
            TextView colonnaData = new TextView(context);
            colonnaData.setTextColor(Color.BLACK);
            colonnaData.setTextSize(24);
            colonnaData.setText("Data");
            colonnaData.setTypeface(null, Typeface.BOLD_ITALIC);
            //colonnaData.setAllCaps(true);
            rigaIntestazione.addView(colonnaData, tableRowParams);


            TextView colonnaNome = new TextView(context);
            colonnaNome.setTextColor(Color.BLACK);
            colonnaNome.setTextSize(24);
            colonnaNome.setText("Nome Lobby");
            colonnaNome.setTypeface(null, Typeface.BOLD_ITALIC);
            //colonnaNome.setAllCaps(true);
            rigaIntestazione.addView(colonnaNome, tableRowParams);


            TextView colonnaPunteggio = new TextView(context);
            colonnaPunteggio.setTextColor(Color.BLACK);
            colonnaPunteggio.setTextSize(24);
            colonnaPunteggio.setText("Vincitore");
            colonnaPunteggio.setTypeface(null, Typeface.BOLD_ITALIC);
            //colonnaPunteggio.setAllCaps(true);
            rigaIntestazione.addView(colonnaPunteggio, tableRowParams);
            tableDashboard.addView(rigaIntestazione, tableRowParams);
            List<Partita> partiteLista = storicoPartiteLista; //nuovo array che ci mostra nella tabella
            for (Partita p1 : partiteLista) {
                TableRow riga = new TableRow(context);
                TextView colonna1 = new TextView(context);
                colonna1.setTextColor(Color.BLACK);
                colonna1.setTextSize(14);
                colonna1.setText(p1.dataStart);
                riga.addView(colonna1, tableRowParams);

                TextView colonna2 = new TextView(context);
                colonna2.setTextColor(Color.BLACK);
                colonna2.setTextSize(14);
                colonna2.setText(p1.nomePartita);
                riga.addView(colonna2, tableRowParams);

                TextView colonna3 = new TextView(context);
                colonna3.setTextColor(Color.BLACK);
                colonna3.setTextSize(14);
                //colonna3.setText("0");
                colonna3.setText(p1.idUserWinner);
                riga.addView(colonna3, tableRowParams);

                tableDashboard.addView(riga, tableRowParams);
            }

        }
    }

    //svuotiamo le collection partite
    public void onClickClearCollection(View view) {
        List<Partita> partiteLista= userSession.partiteLista;
        if(partiteLista.size() == 0){
            Toast t = Toast.makeText(context, "Errore, nessuna partita trovata", Toast.LENGTH_SHORT);
            t.show();
            return;
        }
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference dbPartite = database.collection("Partite");
        //partita.id (dbPartite.document(partita.id))

        for(Partita p : partiteLista) {
            if (p.idPartita != null) {
                dbPartite.document(p.idPartita).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast t = Toast.makeText(context, "La collection Partita è stata eliminata", Toast.LENGTH_SHORT);
                            //t.show();
                        } else {
                            //Toast t = Toast.makeText(context, "Errore, impossibile eliminare la collection", Toast.LENGTH_SHORT);
                            //t.show();
                        }
                    }
                });
            }
        }
        userSession.partiteLista = new ArrayList<Partita>(); //la inizializziamo vuota
    }
}