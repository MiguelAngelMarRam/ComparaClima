package com.mamr.comparaclima.network;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import com.mamr.comparaclima.models.AemetRespuesta;
import com.mamr.comparaclima.models.Municipio;
import com.mamr.comparaclima.models.PrediccionRespuesta;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AemetApi {

    // Clima: Obtener la URL donde está la predicción
    @GET("opendata/api/prediccion/especifica/municipio/diaria/{id}/")
    Call<AemetRespuesta> getUrlPrediccion(@Path("id") String id, @Query("api_key") String apiKey);

    // Descarga de la URL el clima

    @GET
    Call<List<PrediccionRespuesta>> getClimaFinal(@Url String url);
}