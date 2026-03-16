package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mamr.comparaclima.db.DatabaseHelper;
import com.mamr.comparaclima.utils.Validator; // Importamos el validador

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etEmail, etPassword;
    Button btnRegister;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        db = new DatabaseHelper(this);

        etNombre = findViewById(R.id.editTextRName);
        etEmail = findViewById(R.id.editTextREmail);
        etPassword = findViewById(R.id.editTextRPassword);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validación de campos utilizando la clase Validator
            if (Validator.campoVacio(name) || Validator.campoVacio(email) || Validator.campoVacio(password)) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!Validator.esEmailValido(email)) {
                Toast.makeText(this, "Email no válido", Toast.LENGTH_SHORT).show();
            } else if (!Validator.esPasswordSegura(password)) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            } else {
                // Enviamos nombre, email y password
                if (db.registerUser(name, email, password)) {
                    Toast.makeText(this, "¡Registro con éxito!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Error: El email ya existe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}