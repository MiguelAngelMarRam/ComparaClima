package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private HistorialAdapter adapter;
    private List<String[]> datos = new ArrayList<>();
    private DatabaseReference userHistorialRef;
    private List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Comprobación inicial de red
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Estás en modo desconectado. El historial puede no estar actualizado.", Toast.LENGTH_LONG).show();
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userHistorialRef = FirebaseDatabase.getInstance("https://comparaclima-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users").child(uid).child("historial");
        } else {
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rvHistorial);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistorialAdapter(datos, new HistorialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String[] item) {
                Intent intent = new Intent(HistorialActivity.this, ComparacionActivity.class);
                intent.putExtra("REINTENTAR", true);
                intent.putExtra("C1_NOMBRE", item[0]);
                intent.putExtra("C2_NOMBRE", item[1]);
                intent.putExtra("PRESET_ANTERIOR", item[2]);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(String[] item, int position) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(HistorialActivity.this, "Necesitas internet para borrar registros", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(HistorialActivity.this)
                        .setTitle("Borrar registro")
                        .setMessage("¿Estás seguro de que quieres eliminar esta búsqueda?")
                        .setPositiveButton("Borrar", (dialog, which) -> {
                            userHistorialRef.child(keys.get(position)).removeValue();
                            Toast.makeText(HistorialActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rv.setAdapter(adapter);

        // LEER DATOS DE FIREBASE EN TIEMPO REAL
        userHistorialRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                datos.clear();
                keys.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(HistorialActivity.this, "Tu historial está vacío", Toast.LENGTH_SHORT).show();
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String c1 = ds.child("ciudad1").getValue(String.class);
                    String c2 = ds.child("ciudad2").getValue(String.class);
                    String modo = ds.child("modo").getValue(String.class);
                    String fecha = ds.child("fecha").getValue(String.class);

                    datos.add(new String[]{c1, c2, modo, fecha});
                    keys.add(ds.getKey());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialActivity.this, "Error de sincronización con la nube", Toast.LENGTH_SHORT).show();
            }
        });

        View btnBorrarTodo = findViewById(R.id.btnBorrarTodo);
        if (btnBorrarTodo != null) {
            btnBorrarTodo.setOnClickListener(v -> {
                if (datos.isEmpty()) return;
                if (!isNetworkAvailable()) {
                    Toast.makeText(this, "Conéctate a internet para vaciar el historial", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle("Vaciar historial")
                        .setMessage("Se eliminarán todas tus búsquedas de la nube.")
                        .setPositiveButton("Vaciar todo", (dialog, which) -> {
                            userHistorialRef.removeValue();
                            Toast.makeText(this, "Historial vaciado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}