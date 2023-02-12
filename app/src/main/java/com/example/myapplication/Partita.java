package com.example.myapplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Partita {
    //public User user;
    public ArrayList<String> giocatori; //si ricava il numGiocatori
    public String idPartita;
    public String idUser; //chi crea partita
    public String idUserWinner; //chi ha vinto la partita
    public String nomePartita; //deve essere univoco
    public int stato; //0=avviata, 1=in corso(appena entra un giocatore), 2=finita; -1=interrotta
    public String dataStart; //dataora inizio
    public String dataEnd;//dataora fine
    public int numeroEstratto; //numero estratto che aggiorno su firebase
    //public String username;//username utente...

    public Partita(){
        giocatori  = new ArrayList<String>();
    }

    public boolean addGiocatore(String userId){
        if(!giocatori.contains(userId)){
            giocatori.add(userId); //mi salvo i giocatori nella lista
            return true;
        }
        return false;
    }

}
