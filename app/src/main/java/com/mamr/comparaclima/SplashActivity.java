package com.mamr.comparaclima;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // Referencia a la imagen y carga de la animacion de zoom
        ImageView ivSplash = findViewById(R.id.ivSplash);
        Animation zoom = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        ivSplash.startAnimation(zoom);

        // Esperar 5 segundos y saltar al menu con fundido
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Transicion suave de desvanecimiento entre actividades
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            finish();
        }, 5000);
    }
}