package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mamr.comparaclima.db.DatabaseHelper;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private HistorialAdapter adapter;
    private List<String[]> datos;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        db = new DatabaseHelper(this);
        // Filtrr historial por el usuario logueado
        email = getSharedPreferences("SESION", MODE_PRIVATE).getString("email", "");
        datos = db.obtenerHistorial(email);

        RecyclerView rv = findViewById(R.id.rvHistorial);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistorialAdapter(datos, new HistorialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String[] item) {
                // Reutilizar datos del historial para lanzar una nueva comparación
                Intent intent = new Intent(HistorialActivity.this, ComparacionActivity.class);
                intent.putExtra("REINTENTAR", true);
                intent.putExtra("C1_NOMBRE", item[0]);
                intent.putExtra("C2_NOMBRE", item[1]);
                intent.putExtra("PRESET_ANTERIOR", item[2]);
                startActivity(intent);
                finish();
            }

            @Override
            public void onDeleteClick(String[] item, int position) {
                // Borrar un registro del historial
                new AlertDialog.Builder(HistorialActivity.this)
                        .setTitle("Borrar registro")
                        .setMessage("¿Estás seguro de que quieres eliminar esta búsqueda?")
                        .setPositiveButton("Borrar", (dialog, which) -> {
                            // Sincronizamos borrado en DB y en la lista de la UI
                            db.borrarRegistroHistorial(email, item[0], item[1], item[3]);
                            datos.remove(position);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rv.setAdapter(adapter);

        // Borrar historial completo del usuario
        View btnBorrarTodo = findViewById(R.id.btnBorrarTodo);
        if (btnBorrarTodo != null) {
            btnBorrarTodo.setOnClickListener(v -> {
                if (datos.isEmpty()) return;

                new AlertDialog.Builder(this)
                        .setTitle("Vaciar historial")
                        .setMessage("Se eliminarán todas tus búsquedas. Esta acción no se puede deshacer.")
                        .setPositiveButton("Vaciar todo", (dialog, which) -> {
                            db.borrarTodoElHistorial(email);
                            datos.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Historial vacío", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }
    }
}