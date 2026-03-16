package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import androidx.test.platform.app.InstrumentationRegistry;
import com.mamr.comparaclima.db.DatabaseHelper;
import com.mamr.comparaclima.models.Preset; // Importante para el punto 2.5
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IntegrationTests {
    private DatabaseHelper db;

    @Before
    public void setUp() {
        db = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void testIntegracion_FlujoUsuarioCompleto() {
        String email = "profe@tfg.com";

        // 1. Probamos registro
        db.registerUser("Profesor", email, "pass2026");

        // 2. Probamos login
        assertTrue(db.verificarUsuario(email, "pass2026"));

        // 2.2. Probamos Presets vinculados
        db.insertarOActualizarPreset(email, "Mi Playa", 35, 25, 10, 0, 1);

        // 2.3. Lo recuperamos asegurando que filtramos por ese email
        Preset p = db.obtenerObjetoPreset("Mi Playa", email);
        assertNotNull("El preset debería haberse guardado correctamente", p);
        assertEquals("Mi Playa", p.getNombre());

        // 3. Probamos historial
        db.guardarHistorial(email, "Madrid", "Barcelona", "Madrid", "Solo Clima", "Día único");
        assertFalse(db.obtenerHistorial(email).isEmpty());

        // 4. Limpieza
        db.borrarTodoElHistorial(email);
        assertEquals(0, db.obtenerHistorial(email).size());
    }
}