package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<String[]> listaHistorial;
    private OnItemClickListener listener;

    // Interfaz para gestionar acciones desde el Activity
    public interface OnItemClickListener {
        void onItemClick(String[] registro);
        void onDeleteClick(String[] registro, int position);
    }

    public HistorialAdapter(List<String[]> listaHistorial, OnItemClickListener listener) {
        this.listaHistorial = listaHistorial;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] registro = listaHistorial.get(position);

        // Mapeo de datos: [0]Ciudad1, [1]Ciudad2, [2]Modo, [3]Fecha
        holder.tvCiudades.setText(registro[0] + " vs " + registro[1]);
        holder.tvModo.setText("Modo: " + (registro[2] != null ? registro[2] : "Estándar"));
        holder.tvFecha.setText(registro[3]);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(registro);
        });

        holder.btnBorrar.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(registro, position);
        });
    }

    @Override
    public int getItemCount() { return listaHistorial.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCiudades, tvModo, tvFecha;
        ImageButton btnBorrar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCiudades = itemView.findViewById(R.id.tvCiudadesComparadas);
            tvModo = itemView.findViewById(R.id.tvGanadorHistorial);
            tvFecha = itemView.findViewById(R.id.tvFechaHistorial);
            btnBorrar = itemView.findViewById(R.id.btnBorrarItem);
        }
    }
}