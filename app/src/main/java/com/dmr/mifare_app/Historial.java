package com.dmr.mifare_app;

public class Historial {

    private int id;
    private double precio;
    private String movimiento;
    private String fecha;

    public Historial(){

    }

    public Historial(String fecha, String movimiento, double precio){
        this.precio = precio;
        this.movimiento = movimiento;
        this.fecha = fecha;
    }

    public double getPrecio(){
        return precio;
    }

    public String getMovimiento(){
        return movimiento;
    }

    public String getFecha(){
        return fecha;
    }

}
