package com.mamr.comparaclima.network;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String BASE_URL = "https://opendata.aemet.es/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // CONFIGURACIÓN DE OKHTTP PARA ENTORNOS INSTABLES (EMULADOR)
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // 1. Aumentamos los tiempos al máximo razonable
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)

                    // 2. Forzamos reintentos automáticos si la conexión se corta
                    .retryOnConnectionFailure(true)

                    // 3. INTERCEPTOR DE REINTENTOS MANUAL
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        okhttp3.Response response = null;
                        int tryCount = 0;
                        int maxLimit = 3; // Si falla, lo intenta hasta 3 veces automáticamente

                        while (response == null && tryCount < maxLimit) {
                            try {
                                response = chain.proceed(request);
                            } catch (Exception e) {
                                if (tryCount >= maxLimit - 1) throw e;
                            }
                            tryCount++;
                        }
                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}