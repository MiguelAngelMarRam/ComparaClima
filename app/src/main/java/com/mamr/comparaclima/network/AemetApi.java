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
    // Paso 1 para Municipios: Obtener la URL donde están las ciudades
    @GET("opendata/api/maestro/municipios")
    Call<AemetRespuesta> getUrlMunicipios(@Query("api_key") String apiKey);

    // Paso 1 para Clima: Obtener la URL donde está la predicción
    @GET("opendata/api/prediccion/especifica/municipio/diaria/{id}/")
    Call<AemetRespuesta> getUrlPrediccion(@Path("id") String id, @Query("api_key") String apiKey);

    // Paso 2: Esta descarga la lista real de la URL que nos den (sea de ciudades o de clima)
    @GET
    Call<List<Municipio>> getMunicipiosReales(@Url String url);

    @GET
    Call<List<PrediccionRespuesta>> getClimaFinal(@Url String url);
}