package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mamr.comparaclima.models.Preset;

public class AjustesGustos extends AppCompatActivity {

    private EditText etNombre;
    private SeekBar sbTempMax, sbTempMin, sbViento, sbLluvia;
    private TextView tvTempMax, tvTempMin, tvViento, tvLluvia;
    private CheckBox cbSol;
    private Button btnGuardar;

    // --- VARIABLES FIREBASE ---
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes_gustos);

        // Comprobación inicial de red
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Modo desconectado: los cambios se guardarán al recuperar la conexión.", Toast.LENGTH_LONG).show();
        }

        // Inicializamos Firebase Auth
        mDatabase = FirebaseDatabase.getInstance("https://comparaclima-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombrePreset);
        sbTempMax = findViewById(R.id.sbTempMax);
        sbTempMin = findViewById(R.id.sbTempMin);
        sbViento = findViewById(R.id.sbViento);
        sbLluvia = findViewById(R.id.sbLluvia);
        tvTempMax = findViewById(R.id.tvLabelTempMax);
        tvTempMin = findViewById(R.id.tvLabelTempMin);
        tvViento = findViewById(R.id.tvLabelViento);
        tvLluvia = findViewById(R.id.tvLabelLluvia);
        cbSol = findViewById(R.id.cbSol);
        btnGuardar = findViewById(R.id.btnGuardarGusto);
    }

    private void setupListeners() {
        // Actualización dinámica de etiquetas
        sbTempMax.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTempMax.setText("Temperatura Máxima Ideal: " + progress + "°C");
            }
        });

        sbTempMin.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTempMin.setText("Temperatura Mínima Ideal: " + progress + "°C");
            }
        });

        sbViento.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvViento.setText("Viento Máximo: " + progress + " km/h");
            }
        });

        sbLluvia.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvLluvia.setText("Lluvia Máxima permitida: " + progress + "%");
            }
        });

        btnGuardar.setOnClickListener(v -> guardarPresetEnFirebase());
    }

    /**
     * Crea un objeto Preset y lo guarda en la rama 'presets' del usuario actual.
     * Firebase usará el nombre del preset como clave (ID).
     */
    private void guardarPresetEnFirebase() {
        String nombre = etNombre.getText().toString().trim();
        int max = sbTempMax.getProgress();
        int min = sbTempMin.getProgress();

        // Validaciones básicas de lógica climática
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ponle un nombre a tu gusto (ej: Playa)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (min >= max) {
            Toast.makeText(this, "La mínima debe ser menor que la máxima", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegurar que el userId esté disponible
        if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Creamos el objeto modelo
        Preset nuevoGusto = new Preset(
                nombre,
                max,
                min,
                sbViento.getProgress(),
                sbLluvia.getProgress(),
                cbSol.isChecked() ? 1 : 0
        );

        // GUARDADO EN FIREBASE
        if (userId != null) {
            mDatabase.child("users").child(userId).child("presets").child(nombre).setValue(nuevoGusto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AjustesGustos.this, "¡Gusto '" + nombre + "' sincronizado en la nube!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE_ERR", "Error: " + e.getMessage());
                        Toast.makeText(AjustesGustos.this, "Error al guardar en la nube", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}