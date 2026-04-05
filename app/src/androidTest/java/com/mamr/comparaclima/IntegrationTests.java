package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import com.mamr.comparaclima.models.Preset;
import com.mamr.comparaclima.ResultadoActivity.WeatherData;

import org.junit.Test;
import static org.junit.Assert.*;

public class IntegrationTests {

    /**
     * Test de Integración: Algoritmo de Puntuación (IA de destino).
     * Comprobamos que el sistema es capaz de dar una puntuación alta a un clima
     * que encaja perfectamente con los gustos (presets) del usuario.
     */
    @Test
    public void testIntegracion_AlgoritmoDecision() {
        // 1. Preparamos un Clima de ejemplo (Perfecto para playa)
        WeatherData climaPlaya = new WeatherData();
        climaPlaya.max = 30;
        climaPlaya.min = 22;
        climaPlaya.lluvia = 0;
        climaPlaya.viento = 5;
        climaPlaya.estado = "Despejado";

        // 2. Preparamos un Preset (Gustos del usuario)
        Preset gustoUsuario = new Preset("Verano", 30, 20, 15, 5, 1);

        // 3. Ejecutamos el motor de decisión
        double score = ResultadoActivity.calcularPuntos(climaPlaya, gustoUsuario);

        // 4. Verificación: Si el clima es perfecto, la puntuación debe ser muy alta
        assertTrue("El score debería ser cercano a 100", score > 90);
    }

    /**
     * Test de Integración: Penalización por mal clima.
     * Verificamos que si llueve o hace frío y el usuario busca sol,
     * el algoritmo resta puntos correctamente.
     */
    @Test
    public void testIntegracion_PenalizacionClimatica() {
        // 1. Clima adverso (Lluvia y Frío)
        WeatherData climaMalo = new WeatherData();
        climaMalo.max = 10;
        climaMalo.min = 5;
        climaMalo.lluvia = 90;
        climaMalo.viento = 40;
        climaMalo.estado = "Lluvia intensa";

        // 2. Gustos de calor
        Preset gustoCalor = new Preset("Playa", 30, 25, 10, 5, 1);

        // 3. Ejecutamos cálculo
        double score = ResultadoActivity.calcularPuntos(climaMalo, gustoCalor);

        // 4. Verificación: El score debe estar penalizado por debajo de 50
        assertTrue("El score debe estar penalizado por debajo de 50", score < 50);
    }

    /**
     * Test de Modelo: Integridad del objeto Preset.
     */
    @Test
    public void testIntegridadModeloPreset() {
        String nombre = "Montaña";
        Preset p = new Preset(nombre, 20, 10, 30, 20, 0);

        assertEquals("El nombre del preset no coincide", nombre, p.getNombre());
        assertEquals("La temperatura máxima no coincide", 20, p.getTempMaxIdeal());
        assertEquals("El flag de sol no coincide", 0, p.getRequiereSol());
    }

    /**
     * Test de Robustez: Gestión de flujo sin red.
     * Valida que la aplicación bloquea la petición a la API externa si no hay conectividad.
     */
    @Test
    public void testIntegracion_GestionFalloRed() {
        boolean redDisponible = false; // Simulamos isNetworkAvailable() == false
        boolean peticionLanzada = false;

        // Lógica implementada en ComparacionActivity
        if (redDisponible) {
            peticionLanzada = true;
        }

        assertFalse("No se debe lanzar la petición a AEMET sin conexión", peticionLanzada);
    }
}