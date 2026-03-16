package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog; // Importado para la guía
import androidx.appcompat.app.AppCompatActivity;
import com.mamr.comparaclima.db.DatabaseHelper;

public class UsuarioActivity extends AppCompatActivity {

    private TextView tvBienvenida;
    private Button btnIrAComparar, btnIrAPreferencias, btnIrAHistorial, btnCerrarSesion;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        // 1. Inicializar componentes
        db = new DatabaseHelper(this);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        btnIrAComparar = findViewById(R.id.btnComparar);
        btnIrAPreferencias = findViewById(R.id.btnConfiguracion);
        btnIrAHistorial = findViewById(R.id.btnHistorial);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // 2. Recuperar el email
        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String emailUsuario = prefs.getString("email", "");

        // SEGURIDAD: Si el email está vacío, volvemos al login
        if (emailUsuario.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener el nombre desde la BD usando el email
        String nombre = db.getNombreUsuario(emailUsuario);

        tvBienvenida.setText("¡Hola, " + nombre + "!");

        // --- LÓGICA DE LA GUÍA INTERACTIVA (Punto 1 del enunciado) ---
        // Usamos un archivo de preferencias distinto para el control de la guía
        SharedPreferences guiaPrefs = getSharedPreferences("CONFIG_GUIA", MODE_PRIVATE);
        boolean esPrimeraVez = guiaPrefs.getBoolean("mostrar_guia_" + emailUsuario, true);

        if (esPrimeraVez) {
            mostrarGuiaInteractiva();
            // Guardamos que ya se ha mostrado para este usuario concreto
            guiaPrefs.edit().putBoolean("mostrar_guia_" + emailUsuario, false).apply();
        }

        // 3. Configurar eventos de clic
        btnIrAComparar.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioActivity.this, ComparacionActivity.class);
            startActivity(intent);
        });

        btnIrAPreferencias.setOnClickListener(v -> {
            Intent intent = new Intent(this, AjustesGustos.class);
            startActivity(intent);
        });

        btnIrAHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(UsuarioActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Muestra un dialogo informativo como guia interactiva para el usuario.
     */
    private void mostrarGuiaInteractiva() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("🚀 ¡Tu viaje comienza aquí!");

        builder.setMessage("Hola, " + tvBienvenida.getText().toString().replace("¡Hola, ", "").replace("!", "") + ".\n" +
                "Para sacar el máximo partido a ComparaClima, sigue estos 3 pasos:\n\n" +
                "📍 1. CONFIGURA: Entra en 'Preferencias' y dinos qué tiempo te gusta.\n\n" +
                "🔎 2. COMPARA: Elige dos destinos y deja que nosotros busquemos por ti.\n\n" +
                "📊 3. REVISA: Consulta tus decisiones pasadas en el 'Historial'.\n\n" +
                "¿Empezamos?");

        builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setPositiveButton("¡VAMOS ALLÁ!", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.show();
    }
}