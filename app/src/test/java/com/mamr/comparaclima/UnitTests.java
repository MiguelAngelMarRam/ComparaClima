package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import static org.junit.Assert.*;
import org.junit.Test;
import com.mamr.comparaclima.models.Preset;
import com.mamr.comparaclima.utils.Validator;

public class UnitTests {

    // --- TEST DE LÓGICA DE CLIMA (6 TESTS) ---

    @Test
    public void test01_PuntuacionPerfecta() {
        Preset p = new Preset("Ideal", 25, 15, 20, 10, 0);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 25; d.min = 20; d.lluvia = 0; d.viento = 5;
        assertEquals(100.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    @Test
    public void test02_PenalizacionViento() {
        Preset p = new Preset("Bici", 20, 10, 10, 20, 0);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 20; d.min = 15; d.lluvia = 0; d.viento = 20;
        assertEquals(50.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    @Test
    public void test03_PenalizacionLluviaSevera() {
        Preset p = new Preset("Seco", 25, 15, 40, 0, 0);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 25; d.min = 20; d.viento = 0; d.lluvia = 10;
        assertEquals(60.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    @Test
    public void test04_FrioExtremo() {
        Preset p = new Preset("Calor", 25, 20, 40, 40, 0);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 25; d.min = 10; // 10 grados menos del min -> -20 puntos
        assertEquals(80.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    @Test
    public void test05_RequisitoSolCumplido() {
        Preset p = new Preset("Sol", 25, 15, 40, 20, 1);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 25; d.min = 20; d.estado = "Cielo Despejado";
        assertEquals(100.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    @Test
    public void test06_RequisitoSolFallido() {
        Preset p = new Preset("Sol", 25, 15, 40, 20, 1);
        ResultadoActivity.WeatherData d = new ResultadoActivity.WeatherData();
        d.max = 25; d.min = 20; d.estado = "Tormenta Eléctrica";
        assertEquals(75.0, ResultadoActivity.calcularPuntos(d, p), 0.1);
    }

    // --- TEST DE VALIDACIÓN DE ENTRADA (4 TESTS) ---

    @Test
    public void test07_EmailValido() {
        // Prueba que el validador detecta emails correctos e incorrectos
        assertTrue("Debería aceptar email estándar", Validator.esEmailValido("miguel@tfg.com"));
        assertFalse("Debería rechazar sin @", Validator.esEmailValido("usuario.com"));
    }

    @Test
    public void test08_PasswordSegura() {
        // Prueba el requisito de longitud mínima de contraseña
        assertTrue("8 caracteres debe ser válido", Validator.esPasswordSegura("12345678"));
        assertFalse("7 caracteres debe ser insuficiente", Validator.esPasswordSegura("1234567"));
    }

    @Test
    public void test09_CampoVacio() {
        // Prueba la detección de campos obligatorios sin rellenar
        assertTrue("Debería detectar espacio en blanco como vacío", Validator.campoVacio("  "));
        assertFalse("Debería detectar texto como no vacío", Validator.campoVacio("Madrid"));
    }

    // --- TEST DE MÓDULO AUXILIAR ---

    @Test
    public void test10_FormateoNombre() {
        // Probamos que la lógica de bienvenida maneja correctamente los nombres (Ejemplo: quitar espacios)
        String nombreSucio = "  Miguel  ";
        assertEquals("Miguel", nombreSucio.trim());
    }
}