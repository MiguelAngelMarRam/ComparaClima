package com.mamr.comparaclima.models;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Municipio implements Serializable {
    @SerializedName("id")
    private String id;
    @SerializedName("nombre")
    private String nombre;

    public String getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}