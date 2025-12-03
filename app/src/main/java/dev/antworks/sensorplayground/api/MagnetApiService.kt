package dev.antworks.sensorplayground.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

// Data Model for BGS API Response
data class MagnetResponse(
    @SerializedName("geomagnetic-field-model-result")
    val result: GeomagneticResult?
) {
    data class GeomagneticResult(
        @SerializedName("field-value")
        val fieldValue: FieldValue?
    ) {
        data class FieldValue(
            @SerializedName("total-intensity")
            val totalIntensity: Intensity?,
            @SerializedName("declination")
            val declination: Declination?,
            @SerializedName("inclination")
            val inclination: Inclination?
        ) {
            data class Intensity(
                @SerializedName("units")
                val units: String?,
                @SerializedName("value")
                val value: Double?
            )

            data class Declination(
                @SerializedName("units")
                val units: String?,
                @SerializedName("value")
                val value: Double?
            )

            data class Inclination(
                @SerializedName("units")
                val units: String?,
                @SerializedName("value")
                val value: Double?
            )
        }
    }
}

interface MagnetApiService {
    // British Geological Survey (BGS) Geomagnetic Calculator API
    // Free API, no key required
    // Returns magnetic field data using World Magnetic Model (WMM)
    @GET("web_service/GMModels/{model}/{revision}/")
    suspend fun getMagneticField(
        @Path("model") model: String = "wmm",
        @Path("revision") revision: String = "2025",
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("altitude") alt: Double,  // in kilometers!
        @Query("date") date: String,
        @Query("format") format: String = "json"
    ): Response<MagnetResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://geomag.bgs.ac.uk/"

    val instance: MagnetApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MagnetApiService::class.java)
    }
}