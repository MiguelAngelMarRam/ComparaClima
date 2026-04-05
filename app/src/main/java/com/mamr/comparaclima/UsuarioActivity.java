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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioActivity extends AppCompatActivity {

    private TextView tvBienvenida;
    private Button btnIrAComparar, btnIrAPreferencias, btnIrAHistorial, btnCerrarSesion;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // SEGURIDAD: Si no hay usuario, al Login
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvBienvenida = findViewById(R.id.tvBienvenida);
        btnIrAComparar = findViewById(R.id.btnComparar);
        btnIrAPreferencias = findViewById(R.id.btnConfiguracion);
        btnIrAHistorial = findViewById(R.id.btnHistorial);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Recuperamos el nombre desde Firebase
        String nombre = user.getDisplayName();
        if (nombre == null || nombre.isEmpty()) nombre = "Usuario";
        tvBienvenida.setText("¡Hola, " + nombre + "!");

        // Lógica de la Guía
        SharedPreferences guiaPrefs = getSharedPreferences("CONFIG_GUIA", MODE_PRIVATE);
        boolean esPrimeraVez = guiaPrefs.getBoolean("mostrar_guia_" + user.getUid(), true);

        if (esPrimeraVez) {
            mostrarGuiaInteractiva(nombre);
            guiaPrefs.edit().putBoolean("mostrar_guia_" + user.getUid(), false).apply();
        }

        // Eventos
        btnIrAComparar.setOnClickListener(v -> startActivity(new Intent(this, ComparacionActivity.class)));
        btnIrAPreferencias.setOnClickListener(v -> startActivity(new Intent(this, AjustesGustos.class)));
        btnIrAHistorial.setOnClickListener(v -> startActivity(new Intent(this, HistorialActivity.class)));

        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut(); // Cierre de sesión real en Firebase
            getSharedPreferences("SESION", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarGuiaInteractiva(String nombre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("🚀 ¡Tu viaje comienza aquí!");

        builder.setMessage("Hola, " + nombre + ".\n" +
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