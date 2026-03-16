package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mamr.comparaclima.db.DatabaseHelper;

/**
 * Activity para gestionar la creación y edición de los Gustos.
 * Permite al usuario definir sus umbrales climáticos ideales.
 */
public class AjustesGustos extends AppCompatActivity {

    private EditText etNombre;
    private SeekBar sbTempMax, sbTempMin, sbViento, sbLluvia;
    private TextView tvTempMax, tvTempMin, tvViento, tvLluvia;
    private CheckBox cbSol;
    private Button btnGuardar;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes_gustos);

        db = new DatabaseHelper(this);
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
        // Actualizacion dinamica de etiquetas al mover los deslizadores
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

        btnGuardar.setOnClickListener(v -> guardarPreset());
    }

    private void guardarPreset() {
        String nombre = etNombre.getText().toString().trim();
        int max = sbTempMax.getProgress();
        int min = sbTempMin.getProgress();

        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String emailUsuario = prefs.getString("email", "");

        // Validaciones de entrada
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ponle un nombre a tu gusto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (min >= max) {
            Toast.makeText(this, "La mínima debe ser menor que la máxima", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pasar los parametros a la base de datos
        db.insertarOActualizarPreset(
                emailUsuario,
                nombre,
                max,
                min,
                sbViento.getProgress(),
                sbLluvia.getProgress(),
                cbSol.isChecked() ? 1 : 0
        );

        Toast.makeText(this, "¡Gusto '" + nombre + "' guardado!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Clase auxiliar para no repetir código de Seekbar
    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}