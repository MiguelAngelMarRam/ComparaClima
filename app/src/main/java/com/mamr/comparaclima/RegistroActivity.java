package com.mamr.comparaclima;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mamr.comparaclima.utils.Validator;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etEmail, etPassword;
    Button btnRegister;
    private FirebaseAuth mAuth; // Firebase Instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etNombre = findViewById(R.id.editTextRName);
        etEmail = findViewById(R.id.editTextREmail);
        etPassword = findViewById(R.id.editTextRPassword);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (Validator.campoVacio(name) || Validator.campoVacio(email) || Validator.campoVacio(password)) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!Validator.esEmailValido(email)) {
                Toast.makeText(this, "Email no válido", Toast.LENGTH_SHORT).show();
            } else if (!Validator.esPasswordSegura(password)) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            } else {
                // REGISTRO EN FIREBASE
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Guardar el nombre en el perfil de Firebase
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name).build();
                                user.updateProfile(profileUpdates);

                                // ENVIAR EMAIL DE VERIFICACIÓN
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(this, "¡Registro con éxito! Revisa tu email para verificar la cuenta.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}