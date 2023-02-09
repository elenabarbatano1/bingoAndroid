package com.example.myapplication;



import java.util.ArrayList;
import java.util.List;

public class UserSession {

    public String USER_UID;
    private static UserSession instance;
    public boolean isLogged = false;
    public List<Partita> partiteLista; //ci salviamo la lista delle partite in corso
    public List<Integer> numeriChiamatiLista; //ci salviamo la lista dei numeri che escono
    public List<Integer> numeriCartellaLista; //ci salviamo i numeri generati di una cartella
    public List<Integer> numeriTrovatiInCartellaLista;

    private UserSession(){
        partiteLista= new ArrayList<Partita>();
        numeriChiamatiLista = new ArrayList<Integer>();
        numeriCartellaLista = new ArrayList<Integer>();
        numeriTrovatiInCartellaLista = new ArrayList<Integer>();
    }
    //pattern singleton
    public static synchronized UserSession getInstance(){
        if(instance==null){
            instance=new UserSession();
        }
        return instance;
    }
    //ritorno la partita appena creata o in corso (stato 0-1)
    public Partita getPartita(){
        for(Partita p : partiteLista){
            if(p.idPartita != null && (p.stato == 0 || p.stato == 1)){
                return p;
            }
        }
        return null;
    }
     //aggiungiamo la partita alla lista
    public void addPartita(Partita p){
        partiteLista.add(p);
    }
    public boolean isAdmin(){
        Partita p = getPartita();
        if(p!= null && p.idUser == USER_UID){
            return true;
        }
        return false;
    }


}
