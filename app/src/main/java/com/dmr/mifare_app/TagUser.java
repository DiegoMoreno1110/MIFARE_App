package com.dmr.mifare_app;

public class TagUser {
    private String idFire;
    private String idu;
    private double montoActual;
    private String UID;
    private String tipo;


    public TagUser(String id, int String, double montoActual){
        this.idFire = idFire;
        this.idu = idu;
        this.montoActual = montoActual;

    }

    public TagUser(double montoActual){
        this.montoActual = montoActual;
    }

    public TagUser(double montoActual, String UID, String tipo){
        this.montoActual = montoActual;
        this.UID = UID;
        this.tipo = tipo;
    }


    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setIdFire(String idFire) {
        this.idFire = idFire;
    }

    public void setIdu(String idu) {
        this.idu = idu;
    }

    public void setMontoActual(double montoActual) {
        this.montoActual = montoActual;
    }

    public String getIdFire(){
        return idFire;
    }

    public String getIdu(){
        return idu;
    }

    public double getMontoActual(){
        return montoActual;
    }



}


