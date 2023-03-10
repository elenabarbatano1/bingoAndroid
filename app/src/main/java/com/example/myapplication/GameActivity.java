package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class GameActivity extends AppCompatActivity {
    private static Context context;
    private UserSession userSession;
    private int numEstratto;
    private String status;
    private Switch auto;
    private Button btnHome;
    private TableLayout tableCartella;
    private Button btnBingo;
    private TextView textViewNumeroEstratto;
    private TextView textViewStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Game");
        // Initialize user session
        userSession = UserSession.getInstance();
        if (userSession.isAdmin()) {
            setContentView(R.layout.activity_game_admin);
        } else {
            setContentView(R.layout.activity_game);
            Partita p = userSession.getPartita();
            boolean aggiunto = p.addGiocatore(userSession.USER_UID); //risposta del metodo
            if(aggiunto){
                Common.updatePartita(p);
            }

        }
        context = getApplicationContext(); //abbiamo il contesto

        if (userSession.getPartita() == null) {
            finish();//chiude activity
            return;
        }
        userSession.numeriChiamatiLista.clear();
        userSession.numeriTrovatiInCartellaLista.clear();
        userSession.numeriCartellaLista.clear();

        Partita p = userSession.getPartita();
        //LETTURA
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference docRef = database.collection("Partite").document(p.idPartita);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                    //Toast.makeText(context, "onEvent", Toast.LENGTH_SHORT).show();
                    if (e != null) {
                        System.err.println("Listen failed: " + e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        System.out.println("Current data: " + snapshot.getData());
                    } else {
                        System.out.print("Current data: null");
                    }
                }
            });

        /*
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
        */

        initTable();
         auto = findViewById(R.id.auto);
        if (auto != null) {
            auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b == true) {
                        List<Integer> numeriChiamatiLista = userSession.numeriChiamatiLista;
                        List<Integer> numeriCartellaLista = userSession.numeriCartellaLista;

                        //DOBBIAMO FARE UN METODO PERCH?? VA RICHIAMATO OGNI VOLTA
                        //while(userSession.numeriCartellaLista.size()>= userSession.numeriChiamatiLista.size()){
                        long tempoFinePartita = 300 * 1000; //sono 5 minuti
                         textViewNumeroEstratto = findViewById(R.id.textViewNumeroEstratto);
                        //TextView textViewStatus = findViewById(R.id.textViewStatus);
                        CountDownTimer timer = new CountDownTimer(tempoFinePartita, 600) {
                            public void onTick(long millisUntilFinished) {
                                if (userSession.numeriTrovatiInCartellaLista.size() == userSession.numeriCartellaLista.size()) {
                                    status = "WINNER";
                                    this.cancel();
                                    onClickBingo(null);
                                    return;
                                }
                                onClickEstraiNumero(null);
                                //textViewStatus.setText(userSession.numeriTrovatiInCartellaLista.size()+" / "+ userSession.numeriCartellaLista.size());
                            }

                            public void onFinish() {
                                onClickBingo(null);
                            }
                        };
                        timer.start();
                    }
                }
            });
        }
    }

    public void onClickHome(View view) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }

    public void onClickEstraiNumero(View view) {
        if (!userSession.isAdmin()) {
            textViewNumeroEstratto = findViewById(R.id.textViewNumeroEstratto);
            if(userSession.numeriTrovatiInCartellaLista.size() < userSession.numeriCartellaLista.size()) {
                int val = Common.generateRandom(1, 90);
                numEstratto = val;
                userSession.numeriChiamatiLista.add(val);
                textViewNumeroEstratto.setText(val + "");
                //aggiungere marker sulla tabella dei numeri
                refreshTable();
                textViewStatus = findViewById(R.id.textViewStatus);
                textViewStatus.setText(userSession.numeriTrovatiInCartellaLista.size() + " / " + userSession.numeriCartellaLista.size());
            }else{
                onClickBingo(null);
            }
        } else {
            Toast.makeText(context, "Errore, non sei abilitato a questa operazione",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void initTable() {
        tableCartella = findViewById(R.id.tableCartella);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(30, 20, 20, 10);
        //creo tabella con tanti numeri casuali
        int righe = 5;
        int colonne = 5;
        for (int i = 0; i < righe; i++) {
            TableRow tr1 = new TableRow(context);
            for (int j = 0; j < colonne; j++) {
                int val = Common.generateRandomCartella(1, 90);
                userSession.numeriCartellaLista.add(val);
                if (tableCartella != null) {
                    //creo le colonne

                    TextView colonna1 = new TextView(context);
                    colonna1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            colonna1.setBackgroundResource(R.drawable.cella_onclick2);
                        }
                    });
                    colonna1.setBackgroundResource(R.drawable.cella);
                    colonna1.setTextColor(Color.BLACK);
                    colonna1.setPadding(0, 20, 0, 0);
                    colonna1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    colonna1.setTextSize(24);
                    colonna1.setText(val + "");
                    tr1.addView(colonna1, tableRowParams);

                }
            }
            tableCartella.addView(tr1, tableRowParams);
        }
    }

    private void refreshTable() {
        List<Integer> numeriCartellaLista = userSession.numeriCartellaLista;
        List<Integer> numeriChiamatiLista = userSession.numeriChiamatiLista;

        tableCartella = findViewById(R.id.tableCartella);
        tableCartella.removeAllViews(); //cancello tutto
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(30, 20, 20, 10);
        //creo tabella con tanti numeri casuali
        int righe = 5;
        int colonne = 5;
        int k = 0;
        for (int i = 0; i < righe; i++) {
            TableRow tr1 = new TableRow(context);
            for (int j = 0; j < colonne; j++) {
                int val = numeriCartellaLista.get(k);
                k++;
                //creo le colonne
                TextView colonna1 = new TextView(context);
                if (numeriChiamatiLista.contains(val)) {
                    colonna1.setBackgroundResource(R.drawable.cella_onclick2);
                    if (!userSession.numeriTrovatiInCartellaLista.contains(val)) { //controllo che non ci sia nella lista
                        userSession.numeriTrovatiInCartellaLista.add(val);
                    }
                } else {
                    colonna1.setBackgroundResource(R.drawable.cella);
                }
                colonna1.setTextColor(Color.BLACK);
                colonna1.setPadding(0, 20, 0, 0);
                colonna1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                colonna1.setTextSize(24);
                colonna1.setText(val + "");
                tr1.addView(colonna1, tableRowParams);
            }
            tableCartella.addView(tr1, tableRowParams);
        }
    }

    public void onClickBingo(View view) {
        if (userSession.numeriTrovatiInCartellaLista.size() == userSession.numeriCartellaLista.size()) {
            Toast.makeText(context, "Hai vinto ", Toast.LENGTH_SHORT).show();
            status = "WINNER";
            finish();
            //mostro activity vittoria
            Intent i = new Intent(context, VittoriaActivity.class);
            startActivity(i);
        } else {
            //mostro errore
            Toast.makeText(context, "Hai perso ", Toast.LENGTH_SHORT).show();
            status = "LOSE";
            finish();
            //mostro activity di perdita
            Intent i = new Intent(context, PerditaActivity.class);
            startActivity(i);
        }
        userSession.numeriChiamatiLista.clear();
        userSession.numeriTrovatiInCartellaLista.clear();
        userSession.numeriCartellaLista.clear();
    }

}