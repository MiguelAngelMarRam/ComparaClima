package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mamr.comparaclima.utils.Validator;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin, btnGoRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.editTextLEmail);
        etPassword = findViewById(R.id.editTextLPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnGoRegister = findViewById(R.id.buttonGoToRegistro);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (Validator.campoVacio(email) || Validator.campoVacio(pass)) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // COMPROBACIÓN DE CONEXIÓN
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No hay conexión a internet. No se puede iniciar sesión.", Toast.LENGTH_LONG).show();
                return;
            }

            // LOGIN EN FIREBASE
            btnLogin.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        btnLogin.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // COMPROBAR SI EL EMAIL ESTÁ VERIFICADO
                            if (user != null && user.isEmailVerified()) {
                                // Guardar sesión en SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("SESION", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("email", email);
                                editor.putBoolean("logueado", true);
                                editor.apply();

                                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, UsuarioActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Debes verificar tu email primero. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            // Feedback específico: si hay internet pero falla, son las credenciales
                            Toast.makeText(this, "Error de acceso: Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
        });
    }

    /**
     * Metodo auxiliar para verificar el estado de la red
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}