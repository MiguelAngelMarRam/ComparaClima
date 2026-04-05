package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mamr.comparaclima.models.PrediccionRespuesta;
import com.mamr.comparaclima.models.Preset;

import java.util.HashMap;
import java.util.Map;

public class ResultadoActivity extends AppCompatActivity {

    private TextView tvNombre1, tvNombre2, tvDatosC1, tvDatosC2, tvVeredicto;
    private ImageView ivC1, ivC2;
    private View cardC1, cardC2, cardRes;

    private DatabaseReference mDatabase;
    private String userId;
    private String ganadorNombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        mDatabase = FirebaseDatabase.getInstance("https://comparaclima-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initUI();

        PrediccionRespuesta c1 = (PrediccionRespuesta) getIntent().getSerializableExtra("DATA_C1");
        PrediccionRespuesta c2 = (PrediccionRespuesta) getIntent().getSerializableExtra("DATA_C2");
        boolean esFinde = getIntent().getBooleanExtra("ES_FIN_DE_SEMANA", false);
        String presetName = getIntent().getStringExtra("TIPO_DESTINO");

        if (c1 == null || c2 == null) {
            Toast.makeText(this, "Error al recibir datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WeatherData d1, d2;
        if (esFinde) {
            int idxS = getIntent().getIntExtra("IDX_SAB", 5);
            int idxD = getIntent().getIntExtra("IDX_DOM", 6);
            d1 = extractFinde(c1, idxS, idxD);
            d2 = extractFinde(c2, idxS, idxD);
        } else {
            int idx = getIntent().getIntExtra("DIA_SEL", 0);
            d1 = extractSimple(c1, idx);
            d2 = extractSimple(c2, idx);
        }

        cargarPresetYComparar(c1, c2, d1, d2, presetName, esFinde ? "Fin de semana" : "Día único");
    }

    private void initUI() {
        tvNombre1 = findViewById(R.id.tvCiudad1Nombre);
        tvNombre2 = findViewById(R.id.tvCiudad2Nombre);
        tvDatosC1 = findViewById(R.id.tvDatosDetalladosC1);
        tvDatosC2 = findViewById(R.id.tvDatosDetalladosC2);
        tvVeredicto = findViewById(R.id.tvResultadoComparacion);
        ivC1 = findViewById(R.id.ivIconoC1);
        ivC2 = findViewById(R.id.ivIconoC2);

        try {
            cardC1 = (View) tvDatosC1.getParent();
            cardC2 = (View) tvDatosC2.getParent();
            cardRes = (View) tvVeredicto.getParent();
        } catch (Exception e) {
            Log.e("UI_INIT", "Error al asignar vistas: " + e.getMessage());
        }

        findViewById(R.id.btnFinalizar).setOnClickListener(v -> finish());
    }

    private void cargarPresetYComparar(PrediccionRespuesta c1, PrediccionRespuesta c2,
                                       WeatherData d1, WeatherData d2, String pName, String periodo) {

        tvVeredicto.setText("Analizando mejor opción...");

        if (pName == null || pName.equals("Solo Clima")) {
            Preset pDefault = new Preset("Solo Clima", 25, 15, 40, 20, 0);
            ejecutarLogicaComparacion(c1, c2, d1, d2, pDefault, "Solo Clima", periodo);
            return;
        }

        if (userId != null) {
            mDatabase.child("users").child(userId).child("presets").child(pName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Preset p = snapshot.getValue(Preset.class);
                            if (p == null) p = new Preset(pName, 25, 15, 40, 20, 0);
                            ejecutarLogicaComparacion(c1, c2, d1, d2, p, pName, periodo);
                        }

                        @Override public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ResultadoActivity.this, "Fallo al cargar preferencias", Toast.LENGTH_SHORT).show();
                            Preset pError = new Preset(pName, 25, 15, 40, 20, 0);
                            ejecutarLogicaComparacion(c1, c2, d1, d2, pError, pName, periodo);
                        }
                    });
        }
    }

    private void ejecutarLogicaComparacion(PrediccionRespuesta c1, PrediccionRespuesta c2,
                                           WeatherData d1, WeatherData d2, Preset p, String pName, String periodo) {
        tvNombre1.setText(c1.getNombre());
        tvNombre2.setText(c2.getNombre());

        pintarDatos(tvDatosC1, ivC1, d1);
        pintarDatos(tvDatosC2, ivC2, d2);

        double s1 = calcularPuntos(d1, p);
        double s2 = calcularPuntos(d2, p);

        ganadorNombre = (s1 >= s2) ? c1.getNombre() : c2.getNombre();
        tvVeredicto.setText("El mejor destino para " + pName + " es:\n" + ganadorNombre.toUpperCase());

        registrarEnHistorialFirebase(c1.getNombre(), c2.getNombre(), ganadorNombre, pName, periodo);
        aplicarAnimacion();
    }

    public static double calcularPuntos(WeatherData d, Preset p) {
        double score = 100.0;
        score -= Math.abs(d.max - p.getTempMaxIdeal()) * 3;
        if (d.min < p.getTempMinIdeal()) score -= (p.getTempMinIdeal() - d.min) * 2;
        if (d.lluvia > p.getLluviaMax()) score -= (d.lluvia - p.getLluviaMax()) * 5;
        else score -= d.lluvia * 1.5;
        if (d.viento > p.getVientoMax()) score -= (d.viento - p.getVientoMax()) * 4;

        if (p.getRequiereSol() == 1 && d.estado != null) {
            String desc = d.estado.toLowerCase();
            if (!desc.contains("despejado") && !desc.contains("poco nuboso")) score -= 30;
        }
        return score;
    }

    private void registrarEnHistorialFirebase(String c1, String c2, String ganador, String modo, String periodo) {
        if (userId == null) return;
        Map<String, Object> log = new HashMap<>();
        log.put("ciudad1", c1);
        log.put("ciudad2", c2);
        log.put("ganador", ganador);
        log.put("preset", modo);
        log.put("periodo", periodo);
        log.put("timestamp", System.currentTimeMillis());
        mDatabase.child("users").child(userId).child("historial_comparaciones").push().setValue(log);
    }

    private void aplicarAnimacion() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        if (cardC1 != null) cardC1.startAnimation(fadeIn);
        if (cardC2 != null) cardC2.startAnimation(fadeIn);
        if (cardRes != null) cardRes.startAnimation(fadeIn);
    }

    private void pintarDatos(TextView tv, ImageView iv, WeatherData d) {
        String info = "🌡️ Máx: " + d.max + "° / Mín: " + d.min + "°\n\n" +
                "💧 Prob. Lluvia: " + d.lluvia + "%\n\n" +
                "💨 Viento: " + d.viento + " km/h\n\n" +
                "✨ " + (d.estado != null ? d.estado : "N/D");
        tv.setText(info);

        if (d.estado == null) {
            iv.setImageResource(R.drawable.ic_clima_nubes_sol);
            return;
        }
        String desc = d.estado.toLowerCase();
        if (desc.contains("despejado") || desc.contains("sol")) iv.setImageResource(R.drawable.ic_clima_sol);
        else if (desc.contains("lluvia") || desc.contains("chubasco")) iv.setImageResource(R.drawable.ic_clima_lluvia);
        else if (desc.contains("nuboso") || desc.contains("cubierto")) iv.setImageResource(R.drawable.ic_clima_nubes);
        else iv.setImageResource(R.drawable.ic_clima_nubes_sol);
    }

    private static int parseIntSeguro(String valor) {
        if (valor == null || valor.isEmpty()) return 0;
        try {
            if (valor.contains("-")) valor = valor.split("-")[0];
            return Integer.parseInt(valor.replaceAll("[^0-9]", ""));
        } catch (Exception e) { return 0; }
    }

    private static WeatherData extractSimple(PrediccionRespuesta city, int idx) {
        WeatherData wd = new WeatherData();
        try {
            if (city.getPrediccion() == null || city.getPrediccion().getDia().size() <= idx) return wd;
            PrediccionRespuesta.Dia d = city.getPrediccion().getDia().get(idx);
            wd.max = d.getTemperatura().getMaxima();
            wd.min = d.getTemperatura().getMinima();
            if (!d.getProbPrecipitacion().isEmpty()) {
                wd.lluvia = parseIntSeguro(d.getProbPrecipitacion().get(0).getValue());
            }
            if (!d.getViento().isEmpty()) wd.viento = d.getViento().get(0).getVelocidad();
            if (!d.getEstadoCielo().isEmpty()) wd.estado = d.getEstadoCielo().get(0).getDescripcion();
        } catch (Exception e) { Log.e("EXTRACTOR", "Error simple: " + e.getMessage()); }
        return wd;
    }

    public static WeatherData extractFinde(PrediccionRespuesta city, int s, int d) {
        WeatherData wd = new WeatherData();
        try {
            PrediccionRespuesta.Dia diaS = city.getPrediccion().getDia().get(s);
            PrediccionRespuesta.Dia diaD = city.getPrediccion().getDia().get(d);
            wd.max = (diaS.getTemperatura().getMaxima() + diaD.getTemperatura().getMaxima()) / 2;
            wd.min = (diaS.getTemperatura().getMinima() + diaD.getTemperatura().getMinima()) / 2;
            int pS = parseIntSeguro(diaS.getProbPrecipitacion().get(0).getValue());
            int pD = parseIntSeguro(diaD.getProbPrecipitacion().get(0).getValue());
            wd.lluvia = (pS + pD) / 2;
            wd.viento = (diaS.getViento().get(0).getVelocidad() + diaD.getViento().get(0).getVelocidad()) / 2;
            String estS = (!diaS.getEstadoCielo().isEmpty()) ? diaS.getEstadoCielo().get(0).getDescripcion() : "N/D";
            String estD = (!diaD.getEstadoCielo().isEmpty()) ? diaD.getEstadoCielo().get(0).getDescripcion() : "N/D";
            wd.estado = "Sáb: " + estS + "\nDom: " + estD;
        } catch (Exception e) { Log.e("EXTRACTOR", "Error finde: " + e.getMessage()); }
        return wd;
    }

    public static class WeatherData {
        public int max = 0, min = 0, lluvia = 0, viento = 0;
        public String estado = "N/D";
    }
}