package com.mamr.comparaclima;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprobamos sesión para decidir la pantalla de inicio
        SharedPreferences prefs = getSharedPreferences("SESION", Context.MODE_PRIVATE);
        boolean logueado = prefs.getBoolean("logueado", false);

        Intent intent;
        if (logueado) {
            // Cambiar a UsuarioActivity cuando este lista
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Cerramos esta Activity para que no quede en el historial de navegacion
    }
}