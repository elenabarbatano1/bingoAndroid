package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        context = getApplicationContext(); //abbiamo il contesto

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

        if (userSession.getPartita() == null) {
            finish();//chiude activity
            return;
        }

        textViewNumeroEstratto = findViewById(R.id.textViewNumeroEstratto);
        auto = findViewById(R.id.auto);
        tableCartella = findViewById(R.id.tableCartella);

        userSession.numeriChiamatiLista.clear();
        userSession.numeriTrovatiInCartellaLista.clear();
        userSession.numeriCartellaLista.clear();

        initTable();

        Partita p = userSession.getPartita();
        //Listener della vittoria o perdita
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference docRef = database.collection("Partite").document(p.idPartita);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                //Toast.makeText(context, "onEvent", Toast.LENGTH_SHORT).show();
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists() && status == "") {
                    Partita partitaInCorso = snapshot.toObject(Partita.class);
                    //System.out.println("Current data: " + snapshot.getData()); //manda tutto il documento o quello modificato
                   // Toast.makeText(context, "onEvent: "+partitaInCorso.idUserWinner, Toast.LENGTH_LONG).show();
                    if(partitaInCorso.stato == 2){
                        if(userSession.USER_UID.equals(partitaInCorso.idUserWinner)){
                            finish();
                            //mostro activity vittoria
                            Intent i = new Intent(context, VittoriaActivity.class);
                            startActivity(i);
                        } else {
                            finish();
                            //mostro activity di perdita
                            Intent i = new Intent(context, PerditaActivity.class);
                            startActivity(i);
                        }
                    } else if(!userSession.isAdmin() && partitaInCorso.numeroEstratto > 0){
                        int val = partitaInCorso.numeroEstratto;
                        textViewNumeroEstratto.setText(""+ val);
                    }
                } else {
                    //System.out.print("Current data: null");
                }
            }
        });

        if (auto != null) {
            auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b == true) {
                        List<Integer> numeriChiamatiLista = userSession.numeriChiamatiLista;
                        List<Integer> numeriCartellaLista = userSession.numeriCartellaLista;

                        //DOBBIAMO FARE UN METODO PERCHè VA RICHIAMATO OGNI VOLTA
                        //while(userSession.numeriCartellaLista.size()>= userSession.numeriChiamatiLista.size()){
                        long tempoFinePartita = 300 * 1000; //sono 5 minuti

                        CountDownTimer timer = new CountDownTimer(tempoFinePartita, 600) {
                            public void onTick(long millisUntilFinished) {
                                if (userSession.numeriTrovatiInCartellaLista.size() == userSession.numeriCartellaLista.size()) {
                                    this.cancel();
                                    onClickBingo(null);
                                    return;
                                }
                                onClickEstraiNumero(null);
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

    @Override
    public void onBackPressed() {
        Partita p = userSession.getPartita(); //da la partita in corso
        if (p.giocatori.size() <= 1) {
            //finish();
            super.onBackPressed();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Uscendo perderai la partita. Sei sicuro di voler uscire?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();

                //Partita p = userSession.getPartita(); //ci da la partita in corso
                if (p != null) {
                    DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    p.dataEnd = dt.format(LocalDateTime.now()); //cambiamo data
                    p.stato = -1; //cambiamo stato
                    Common.updatePartita(p);
                }
            }
        });
        builder.setNegativeButton("NO",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    public void onClickHome(View view)
    {
        Partita p = userSession.getPartita(); //da la partita in corso
        if (p.giocatori.size() > 1) {
            onBackPressed();
            return;
        }

        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
    }

    public void onClickEstraiNumero(View view) {
        if (userSession.isAdmin()) {
            if(userSession.numeriTrovatiInCartellaLista.size() < userSession.numeriCartellaLista.size()) {
                int val = Common.generateRandom(1, 90);
                numEstratto = val;
                userSession.numeriChiamatiLista.add(val);
                textViewNumeroEstratto.setText(val + "");
                //aggiungere marker sulla tabella dei numeri
                refreshTable();

                //lancio update di firebase
                Partita p = userSession.getPartita(); //da la partita in corso
                p.numeroEstratto = val;
                if(val > 0){
                    Common.updatePartita(p);
                }
            }else{
                onClickBingo(null);
            }
        } else {
            Toast.makeText(context, "Errore, non sei abilitato a questa operazione",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void initTable() 
    {
        if (tableCartella == null)
            return;

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

    private void refreshTable() 
    {
        if (tableCartella == null)
            return;

        List<Integer> numeriCartellaLista = userSession.numeriCartellaLista;
        List<Integer> numeriChiamatiLista = userSession.numeriChiamatiLista;

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

    public void onClickBingo(View view) 
    {
        Partita p = userSession.getPartita(); //da la partita in corso
        if (p.giocatori.size() <= 1) {
            Toast.makeText(context, "ERRORE: Numero giocatori minimo 2!!!", Toast.LENGTH_LONG).show();
            return;
        }

        /*
        if (userSession.numeriTrovatiInCartellaLista.size() == userSession.numeriCartellaLista.size()) {
            for (Integer numero : userSession.numeriCartellaLista) {
                if (userSession.numeriTrovatiInCartellaLista.contains(numero)) {
                    //fai controllo vittoria
                    /*for numeri trovati in cartella
                     * i = numeri estratti cartella
                     * else hai perso*
                    Toast.makeText(context, "Hai vinto ", Toast.LENGTH_SHORT).show();
                    status = "WINNER";
                    finish();
                    //mostro activity vittoria
                    Intent i = new Intent(context, VittoriaActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(context, "Hai perso, i numeri non corrispondono ", Toast.LENGTH_SHORT).show();
                    status = "LOSE";
                    finish();
                    //mostro activity di perdita
                    Intent i = new Intent(context, PerditaActivity.class);
                    startActivity(i);
                }
            }
        }
        */


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