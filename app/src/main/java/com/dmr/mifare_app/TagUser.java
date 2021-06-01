package com.dmr.mifare_app;

public class TagUser {
    private String idFire;
    private String idu;
    private double montoActual;



    private String UID;
    private String tipo;
    private int sector;
    private String llave;
    private int bloque;


    public TagUser(String id, int String, double montoActual){
        this.idFire = idFire;
        this.idu = idu;
        this.montoActual = montoActual;

    }

    public TagUser(String UID, String tipo, int sector, String llave, int bloque) {
        this.UID = UID;
        this.tipo = tipo;
        this.sector = sector;
        this.llave = llave;
        this.bloque = bloque;
    }

    public TagUser(double montoActual){
        this.montoActual = montoActual;
    }

    public TagUser(double montoActual, String UID, String tipo){
        this.montoActual = montoActual;
        this.UID = UID;
        this.tipo = tipo;
    }

    public TagUser(String UID, String tipo){
        this.UID = UID;
        this.tipo = tipo;
    }

    public TagUser(int sector, String llave){
        this.sector = sector;
        this.llave = llave;
    }


    public int getBloque() {
        return bloque;
    }

    public void setBloque(int bloque) {
        this.bloque = bloque;
    }


    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public String getLlave() {
        return llave;
    }

    public void setLlave(String llave) {
        this.llave = llave;
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


