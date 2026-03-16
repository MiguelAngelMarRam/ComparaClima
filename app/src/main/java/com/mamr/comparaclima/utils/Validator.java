package com.mamr.comparaclima.utils;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

public class Validator {
    public static boolean esEmailValido(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public static boolean esPasswordSegura(String pass) {
        return pass != null && pass.length() >= 8;
    }

    public static boolean campoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}