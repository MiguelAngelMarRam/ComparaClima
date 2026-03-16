package com.mamr.comparaclima.models;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class PrediccionRespuesta implements Serializable {

    private String nombre;
    private Prediccion prediccion;

    public String getNombre() { return nombre; }
    public Prediccion getPrediccion() { return prediccion; }

    public static class Prediccion implements Serializable {
        private List<Dia> dia;
        public List<Dia> getDia() { return dia; }
    }

    public static class Dia implements Serializable {
        private String fecha;
        private Temperatura temperatura;

        @SerializedName("probPrecipitacion")
        private List<ProbPrecipitacion> probPrecipitacion;

        @SerializedName("estadoCielo")
        private List<EstadoCielo> estadoCielo;

        @SerializedName("viento")
        private List<Viento> viento;

        public Temperatura getTemperatura() { return temperatura; }
        public List<ProbPrecipitacion> getProbPrecipitacion() { return probPrecipitacion; }
        public List<EstadoCielo> getEstadoCielo() { return estadoCielo; }
        public List<Viento> getViento() { return viento; }
    }

    public static class Temperatura implements Serializable {
        private int maxima;
        private int minima;
        public int getMaxima() { return maxima; }
        public int getMinima() { return minima; }
    }

    public static class ProbPrecipitacion implements Serializable {
        private String value;
        public String getValue() { return (value == null || value.isEmpty()) ? "0" : value; }
    }

    public static class EstadoCielo implements Serializable {
        private String descripcion;
        private String value;                                           // Código del icono
        public String getDescripcion() { return descripcion; }
        public String getValue() { return value; }
    }

    public static class Viento implements Serializable {
        private int velocidad;
        public int getVelocidad() { return velocidad; }
    }
}