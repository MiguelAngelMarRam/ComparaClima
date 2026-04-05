package com.mamr.comparaclima.models;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

public class Preset implements Serializable {
    private String nombre;
    private int tempMaxIdeal;
    private int tempMinIdeal;
    private int vientoMax;
    private int lluviaMax;
    private int requiereSol;

    // CONSTRUCTOR VACÍO
    public Preset() {}

    public Preset(String nombre, int tempMaxIdeal, int tempMinIdeal, int vientoMax, int lluviaMax, int requiereSol) {
        this.nombre = nombre;
        this.tempMaxIdeal = tempMaxIdeal;
        this.tempMinIdeal = tempMinIdeal;
        this.vientoMax = vientoMax;
        this.lluviaMax = lluviaMax;
        this.requiereSol = requiereSol;
    }

    // Getters y Setters
    @PropertyName("nombre")
    public String getNombre() { return nombre; }
    @PropertyName("nombre")
    public void setNombre(String nombre) { this.nombre = nombre; }

    @PropertyName("tempMaxIdeal")
    public int getTempMaxIdeal() { return tempMaxIdeal; }
    @PropertyName("tempMaxIdeal")
    public void setTempMaxIdeal(int tempMaxIdeal) { this.tempMaxIdeal = tempMaxIdeal; }

    @PropertyName("tempMinIdeal")
    public int getTempMinIdeal() { return tempMinIdeal; }
    @PropertyName("tempMinIdeal")
    public void setTempMinIdeal(int tempMinIdeal) { this.tempMinIdeal = tempMinIdeal; }

    @PropertyName("vientoMax")
    public int getVientoMax() { return vientoMax; }
    @PropertyName("vientoMax")
    public void setVientoMax(int vientoMax) { this.vientoMax = vientoMax; }

    @PropertyName("lluviaMax")
    public int getLluviaMax() { return lluviaMax; }
    @PropertyName("lluviaMax")
    public void setLluviaMax(int lluviaMax) { this.lluviaMax = lluviaMax; }

    @PropertyName("requiereSol")
    public int getRequiereSol() { return requiereSol; }
    @PropertyName("requiereSol")
    public void setRequiereSol(int requiereSol) { this.requiereSol = requiereSol; }

    @Override
    public String toString() { return nombre; }
}