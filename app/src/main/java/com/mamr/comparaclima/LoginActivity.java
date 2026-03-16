package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mamr.comparaclima.db.DatabaseHelper;
import com.mamr.comparaclima.utils.Validator;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin, btnGoRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etEmail = findViewById(R.id.editTextLEmail);
        etPassword = findViewById(R.id.editTextLPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnGoRegister = findViewById(R.id.buttonGoToRegistro);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // Validación de campos vacíos o formatos incorrectos usando Validator
            if (Validator.campoVacio(email) || Validator.campoVacio(pass)) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Validator.esEmailValido(email)) {
                etEmail.setError("Formato de email no válido");
                return;
            }

            if (db.verificarUsuario(email, pass)) {
                // Guardar sesión de forma persistente
                SharedPreferences prefs = getSharedPreferences("SESION", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", email);
                editor.putBoolean("logueado", true);
                editor.commit(); // commit() para asegurar que se guarde antes de pasar de pantalla

                Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, UsuarioActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });
    }
}