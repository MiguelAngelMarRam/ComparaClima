package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mamr.comparaclima.db.DatabaseHelper;
import com.mamr.comparaclima.models.AemetRespuesta;
import com.mamr.comparaclima.models.Municipio;
import com.mamr.comparaclima.models.PrediccionRespuesta;
import com.mamr.comparaclima.network.AemetApi;
import com.mamr.comparaclima.network.RetrofitClient;

import java.io.Serializable;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparacionActivity extends AppCompatActivity {

    private AutoCompleteTextView auto1, auto2;
    private Spinner spinnerDia, spinnerDestino;
    private Button btnComparar;
    private List<Municipio> listaMunicipios = new ArrayList<>();
    private PrediccionRespuesta datosCiudad1, datosCiudad2;
    private boolean pred1rx = false, pred2rx = false; // Flags para controlar la descarga asíncrona doble
    private DatabaseHelper db;

    private final String MI_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaWd1ZWxtcjU2MEBob3RtYWlsLmNvbSIsImp0aSI6IjJjNGM1MmUwLWM3MzItNGUxNy05YTlhLTc2MGY3OWJlMmE5ZSIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNzcxOTUzOTk5LCJ1c2VySWQiOiIyYzRjNTJlMC1jNzMyLTRlMTctOWE5YS03NjBmNzliZTJhOWUiLCJyb2xlIjoiIn0.q3IfSlydAMcJq-6RbUA5dlQSU1hb1ISa7FCTxPRGVMc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparacion);

        db = new DatabaseHelper(this);

        auto1 = findViewById(R.id.autoCompleteCiudad1);
        auto2 = findViewById(R.id.autoCompleteCiudad2);
        btnComparar = findViewById(R.id.btnCompararAhora);
        spinnerDia = findViewById(R.id.spinnerDia);
        spinnerDestino = findViewById(R.id.spinnerDestino);

        configurarSpinnerFechas();
        configurarSpinnerDestinos();
        cargarMunicipiosDesdeAPI();

        // Logica de reintento: recupera datos previos si la Activity se reinicia por error
        if (getIntent().getBooleanExtra("REINTENTAR", false)) {
            auto1.setText(getIntent().getStringExtra("C1_NOMBRE"));
            auto2.setText(getIntent().getStringExtra("C2_NOMBRE"));

            String pAnterior = getIntent().getStringExtra("PRESET_ANTERIOR");
            if (pAnterior != null) {
                ArrayAdapter myAdapter = (ArrayAdapter) spinnerDestino.getAdapter();
                int pos = myAdapter.getPosition(pAnterior);
                if (pos >= 0) spinnerDestino.setSelection(pos);
            }
        }

        btnComparar.setOnClickListener(v -> {
            Municipio m1 = buscarMunicipio(auto1.getText().toString());
            Municipio m2 = buscarMunicipio(auto2.getText().toString());

            if (m1 != null && m2 != null) {
                if (m1.getNombre().equals(m2.getNombre())) {
                    Toast.makeText(this, "Selecciona dos ciudades distintas", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Consultando datos en AEMET...", Toast.LENGTH_SHORT).show();
                btnComparar.setEnabled(false);

                // Reiniciamos estados antes de lanzar las peticiones paralelas
                pred1rx = false;
                pred2rx = false;
                obtenerUrlPrediccion(prepararId(m1.getId()), 1);
                obtenerUrlPrediccion(prepararId(m2.getId()), 2);
            } else {
                Toast.makeText(this, "Selecciona ciudades de la lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Verifica que ambas ciudades hayan descargado sus datos antes de saltar a resultados
    private void comprobarYProcesar() {
        if (pred1rx && pred2rx) {
            btnComparar.setEnabled(true);
            int posicionSeleccionada = spinnerDia.getSelectedItemPosition();
            Intent intent = new Intent(this, ResultadoActivity.class);
            intent.putExtra("DATA_C1", (Serializable) datosCiudad1);
            intent.putExtra("DATA_C2", (Serializable) datosCiudad2);
            intent.putExtra("TIPO_DESTINO", spinnerDestino.getSelectedItem().toString());

            // Si el usuario elige "Fin de semana" (pos 7), enviamos los índices calculados
            if (posicionSeleccionada == 7) {
                int[] indices = calcularIndicesFinDeSemana();
                intent.putExtra("ES_FIN_DE_SEMANA", true);
                intent.putExtra("IDX_SAB", indices[0]);
                intent.putExtra("IDX_DOM", indices[1]);
            } else {
                intent.putExtra("ES_FIN_DE_SEMANA", false);
                intent.putExtra("DIA_SEL", posicionSeleccionada);
            }
            startActivity(intent);
        }
    }

    // Primer paso AEMET: Obtener la URL temporal donde están los datos de municipios
    private void cargarMunicipiosDesdeAPI() {
        AemetApi api = RetrofitClient.getClient().create(AemetApi.class);
        api.getUrlMunicipios(MI_API_KEY).enqueue(new Callback<AemetRespuesta>() {
            @Override
            public void onResponse(Call<AemetRespuesta> call, Response<AemetRespuesta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    descargarListaMunicipiosFinal(response.body().getDatos());
                    Toast.makeText(ComparacionActivity.this, "Actualizando lista de ciudades...", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<AemetRespuesta> call, Throwable t) {
                Toast.makeText(ComparacionActivity.this, "Error de red con AEMET", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Segundo paso AEMET: Descarga del JSON con los nombres e IDs de todos los municipios
    private void descargarListaMunicipiosFinal(String urlReal) {
        AemetApi api = RetrofitClient.getClient().create(AemetApi.class);
        api.getMunicipiosReales(urlReal).enqueue(new Callback<List<Municipio>>() {
            @Override
            public void onResponse(Call<List<Municipio>> call, Response<List<Municipio>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMunicipios = response.body();
                    runOnUiThread(() -> {
                        MunicipioAdapter adapter = new MunicipioAdapter(ComparacionActivity.this, listaMunicipios);
                        auto1.setAdapter(adapter);
                        auto2.setAdapter(adapter);
                        Toast.makeText(ComparacionActivity.this, "Ciudades cargadas correctamente", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override public void onFailure(Call<List<Municipio>> call, Throwable t) {}
        });
    }

    // Descarga la predicción meteorológica final para la ciudad indicada por index (1 o 2)
    private void descargarClimaFinal(String urlReal, int index) {
        AemetApi api = RetrofitClient.getClient().create(AemetApi.class);
        api.getClimaFinal(urlReal).enqueue(new Callback<List<PrediccionRespuesta>>() {
            @Override
            public void onResponse(Call<List<PrediccionRespuesta>> call, Response<List<PrediccionRespuesta>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    if (index == 1) { datosCiudad1 = response.body().get(0); pred1rx = true; }
                    else { datosCiudad2 = response.body().get(0); pred2rx = true; }
                    comprobarYProcesar();
                }
            }
            @Override public void onFailure(Call<List<PrediccionRespuesta>> call, Throwable t) {
                btnComparar.setEnabled(true);
                Toast.makeText(ComparacionActivity.this, "Error en la descarga del clima", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Genera dinámicamente las fechas de los próximos 7 días para el Spinner
    private void configurarSpinnerFechas() {
        List<String> opciones = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String dia = sdf.format(cal.getTime());
            if (i == 0) dia = "Hoy (" + dia + ")";
            else if (i == 1) dia = "Mañana (" + dia + ")";
            opciones.add(dia);
            cal.add(Calendar.DATE, 1);
        }
        opciones.add("Próximo Fin de Semana (Completo)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapter);
    }

    // Carga los perfiles de viaje (Playa, Esquí, etc.) desde la base de datos
    private void configurarSpinnerDestinos() {
        // 1. Recuperamos el email del usuario logueado
        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String emailUsuario = prefs.getString("email", "");

        // 2. Le pasamos el email al metodo para que solo nos traiga sus presets (y los de sistema)
        List<String> destinos = db.obtenerNombresPresets(emailUsuario);
        if (destinos.isEmpty()) destinos.add("Solo Clima");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, destinos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestino.setAdapter(adapter);
    }

    // Calcula cuántos días faltan para el sábado y domingo desde hoy
    private int[] calcularIndicesFinDeSemana() {
        Calendar cal = Calendar.getInstance();
        int diaHoySemana = cal.get(Calendar.DAY_OF_WEEK);
        int diasHastaSabado;
        if (diaHoySemana == Calendar.SATURDAY) diasHastaSabado = 0;
        else if (diaHoySemana == Calendar.SUNDAY) diasHastaSabado = 6;
        else diasHastaSabado = Calendar.SATURDAY - diaHoySemana;
        int idxSabado = Math.min(diasHastaSabado, 6);
        int idxDomingo = Math.min(diasHastaSabado + 1, 6);
        return new int[]{idxSabado, idxDomingo};
    }

    // Obtiene la URL de prediccion especifica para un ID de municipio
    private void obtenerUrlPrediccion(String id, int index) {
        AemetApi api = RetrofitClient.getClient().create(AemetApi.class);
        api.getUrlPrediccion(id, MI_API_KEY).enqueue(new Callback<AemetRespuesta>() {
            @Override
            public void onResponse(Call<AemetRespuesta> call, Response<AemetRespuesta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    descargarClimaFinal(response.body().getDatos(), index);
                }
            }
            @Override public void onFailure(Call<AemetRespuesta> call, Throwable t) {}
        });
    }

    // Elimina acentos y tildes para facilitar la búsqueda de ciudades
    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }

    private Municipio buscarMunicipio(String nombre) {
        String n = normalizar(nombre);
        for (Municipio m : listaMunicipios) {
            if (normalizar(m.getNombre()).equals(n)) return m;
        }
        return null;
    }

    // Formatea el ID para asegurar que siempre tenga 5 dígitos (prefijo con ceros)
    private String prepararId(String id) {
        try { return String.format("%05d", Integer.parseInt(id.replaceAll("[^0-9]", ""))); }
        catch (Exception e) { return id; }
    }

    // Adaptador personalizado para filtrar la lista de autocompletado mientras el usuario escribe
    private class MunicipioAdapter extends ArrayAdapter<Municipio> {
        private final List<Municipio> fullList;
        private List<Municipio> filteredList;

        public MunicipioAdapter(Context context, List<Municipio> objects) {
            super(context, android.R.layout.simple_dropdown_item_1line, objects);
            this.fullList = new ArrayList<>(objects);
            this.filteredList = new ArrayList<>(objects);
        }
        @Override public int getCount() { return filteredList.size(); }
        @Override public Municipio getItem(int pos) { return filteredList.get(pos); }
        @NonNull @Override public Filter getFilter() {
            return new Filter() {
                @Override protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint != null && constraint.length() > 0) {
                        String filterPattern = normalizar(constraint.toString());
                        List<Municipio> startsWith = new ArrayList<>();
                        List<Municipio> contains = new ArrayList<>();
                        for (Municipio m : fullList) {
                            String nombreNorm = normalizar(m.getNombre());
                            if (nombreNorm.startsWith(filterPattern)) startsWith.add(m);
                            else if (nombreNorm.contains(filterPattern)) contains.add(m);
                        }
                        // Priorizamos los que empiezan por la letra escrita antes que los que la contienen
                        List<Municipio> suggestions = new ArrayList<>(startsWith);
                        suggestions.addAll(contains);
                        results.values = suggestions;
                        results.count = suggestions.size();
                    }
                    return results;
                }
                @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList.clear();
                    if (results.values != null) filteredList.addAll((List) results.values);
                    notifyDataSetChanged();
                }
                @Override public CharSequence convertResultToString(Object resultValue) {
                    return ((Municipio) resultValue).getNombre();
                }
            };
        }
    }
}