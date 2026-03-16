package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mamr.comparaclima.db.DatabaseHelper;
import com.mamr.comparaclima.models.PrediccionRespuesta;
import com.mamr.comparaclima.models.Preset;

public class ResultadoActivity extends AppCompatActivity {

    private TextView tvNombre1, tvNombre2, tvDatosC1, tvDatosC2, tvVeredicto;
    private ImageView ivC1, ivC2;
    private View cardC1, cardC2, cardRes;
    private DatabaseHelper db;
    private String ganadorNombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        db = new DatabaseHelper(this);
        initUI();

        PrediccionRespuesta c1 = (PrediccionRespuesta) getIntent().getSerializableExtra("DATA_C1");
        PrediccionRespuesta c2 = (PrediccionRespuesta) getIntent().getSerializableExtra("DATA_C2");
        boolean esFinde = getIntent().getBooleanExtra("ES_FIN_DE_SEMANA", false);
        String presetName = getIntent().getStringExtra("TIPO_DESTINO");

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

        if (c1 != null && c2 != null) {
            procesarIA(c1, c2, d1, d2, presetName);
            String periodo = esFinde ? "Fin de semana" : "Día único";
            registrarEnHistorial(c1.getNombre(), c2.getNombre(), ganadorNombre, presetName, periodo);
            aplicarAnimacion();
        }
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
            cardC1 = (View) ivC1.getParent().getParent();
            cardC2 = (View) ivC2.getParent().getParent();
            cardRes = (View) tvVeredicto.getParent().getParent();
        } catch (Exception e) { }

        View btn = findViewById(R.id.btnFinalizar);
        if (btn != null) btn.setOnClickListener(v -> finish());
    }

    private void procesarIA(PrediccionRespuesta c1, PrediccionRespuesta c2, WeatherData d1, WeatherData d2, String pName) {
        // 1. Recuperamos el email del usuario logueado
        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String emailUsuario = prefs.getString("email", "");

        // 2. Ahora llamamos al metodo
        Preset p = db.obtenerObjetoPreset(pName, emailUsuario);
        if (p == null) p = new Preset(pName, 25, 15, 40, 20, 0);

        tvNombre1.setText(c1.getNombre());
        tvNombre2.setText(c2.getNombre());

        pintarDatos(tvDatosC1, ivC1, d1);
        pintarDatos(tvDatosC2, ivC2, d2);

        double s1 = calcularPuntos(d1, p);
        double s2 = calcularPuntos(d2, p);

        ganadorNombre = (s1 >= s2) ? c1.getNombre() : c2.getNombre();
        tvVeredicto.setText("El mejor destino para " + pName + " es:\n" + ganadorNombre.toUpperCase());
    }

    /**
     * METODO ESTATICO PARA TEST: No requiere instanciar la Activity.
     */
    public static double calcularPuntos(WeatherData d, Preset p) {
        double score = 100.0;
        score -= Math.abs(d.max - p.getTempMaxIdeal()) * 3;
        if (d.min < p.getTempMinIdeal()) {
            score -= (p.getTempMinIdeal() - d.min) * 2;
        }
        if (d.lluvia > p.getLluviaMax()) {
            score -= (d.lluvia - p.getLluviaMax()) * 4;
        } else {
            score -= d.lluvia * 1.5;
        }
        if (d.viento > p.getVientoMax()) {
            score -= (d.viento - p.getVientoMax()) * 5;
        }
        if (p.getRequiereSol() == 1 && d.estado != null) {
            String desc = d.estado.toLowerCase();
            if (!desc.contains("despejado") && !desc.contains("poco nuboso")) {
                score -= 25;
            }
        }
        return score;
    }

    private void registrarEnHistorial(String c1, String c2, String ganador, String modo, String periodo) {
        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        if (!email.isEmpty()) {
            db.guardarHistorial(email, c1, c2, ganador, modo, periodo);
        }
    }

    private void aplicarAnimacion() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        if (cardC1 != null) cardC1.startAnimation(fadeIn);
        if (cardC2 != null) cardC2.startAnimation(fadeIn);
        if (cardRes != null) cardRes.startAnimation(fadeIn);
    }

    private void pintarDatos(TextView tv, ImageView iv, WeatherData d) {
        String info = "🌡️ " + d.max + "° / " + d.min + "°\n\n" +
                "💧 Lluvia: " + d.lluvia + "%\n\n" +
                "💨 Viento: " + d.viento + " km/h\n\n" +
                "✨ " + (d.estado != null ? d.estado : "N/D");
        tv.setText(info);

        if (d.estado == null) return;
        String desc = d.estado.toLowerCase();
        if (desc.contains("despejado") || desc.contains("sol")) {
            iv.setImageResource(R.drawable.ic_clima_sol);
        } else if (desc.contains("nuboso") || desc.contains("cubierto")) {
            iv.setImageResource(R.drawable.ic_clima_nubes);
        } else if (desc.contains("lluvia") || desc.contains("llovizna")) {
            iv.setImageResource(R.drawable.ic_clima_lluvia);
        } else {
            iv.setImageResource(R.drawable.ic_clima_nubes_sol);
        }
    }

    private static WeatherData extractSimple(PrediccionRespuesta city, int idx) {
        WeatherData wd = new WeatherData();
        try {
            PrediccionRespuesta.Dia d = city.getPrediccion().getDia().get(idx);
            wd.max = d.getTemperatura().getMaxima();
            wd.min = d.getTemperatura().getMinima();
            String prob = d.getProbPrecipitacion().get(0).getValue();
            wd.lluvia = Integer.parseInt(prob.isEmpty() ? "0" : prob);
            wd.viento = d.getViento().get(0).getVelocidad();
            wd.estado = d.getEstadoCielo().get(0).getDescripcion();
        } catch (Exception e) { e.printStackTrace(); }
        return wd;
    }

    public static WeatherData extractFinde(PrediccionRespuesta city, int s, int d) {
        WeatherData wd = new WeatherData();
        try {
            PrediccionRespuesta.Dia diaS = city.getPrediccion().getDia().get(s);
            PrediccionRespuesta.Dia diaD = city.getPrediccion().getDia().get(d);
            wd.max = (diaS.getTemperatura().getMaxima() + diaD.getTemperatura().getMaxima()) / 2;
            wd.min = (diaS.getTemperatura().getMinima() + diaD.getTemperatura().getMinima()) / 2;
            int probS = Integer.parseInt(diaS.getProbPrecipitacion().get(0).getValue().isEmpty() ? "0" : diaS.getProbPrecipitacion().get(0).getValue());
            int probD = Integer.parseInt(diaD.getProbPrecipitacion().get(0).getValue().isEmpty() ? "0" : diaD.getProbPrecipitacion().get(0).getValue());
            wd.lluvia = (probS + probD) / 2;
            wd.viento = (diaS.getViento().get(0).getVelocidad() + diaD.getViento().get(0).getVelocidad()) / 2;
            wd.estado = "Promedio Finde";
        } catch (Exception e) { e.printStackTrace(); }
        return wd;
    }

    public static class WeatherData {
        public int max, min, lluvia, viento;
        public String estado;
    }
}