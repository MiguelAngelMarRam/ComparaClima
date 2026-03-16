package com.mamr.comparaclima.models;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import java.io.Serializable;

public class Preset implements Serializable {
    private final String nombre;
    private final int tempMaxIdeal;
    private final int tempMinIdeal;
    private final int vientoMax;
    private final int lluviaMax;
    private final int requiereSol;

    // Constructor completo
    public Preset(String nombre, int tempMaxIdeal, int tempMinIdeal, int vientoMax, int lluviaMax, int requiereSol) {
        this.nombre = nombre;
        this.tempMaxIdeal = tempMaxIdeal;
        this.tempMinIdeal = tempMinIdeal;
        this.vientoMax = vientoMax;
        this.lluviaMax = lluviaMax;
        this.requiereSol = requiereSol;
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getTempMaxIdeal() { return tempMaxIdeal; }
    public int getTempMinIdeal() { return tempMinIdeal; }
    public int getVientoMax() { return vientoMax; }
    public int getLluviaMax() { return lluviaMax; }
    public int getRequiereSol() { return requiereSol; }

    // Metodo toString
    @Override
    public String toString() {
        return nombre;
    }
}