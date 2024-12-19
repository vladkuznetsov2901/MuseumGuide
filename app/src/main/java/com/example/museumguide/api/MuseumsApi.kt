package com.example.museumguide.api

import com.example.museumguide.data.Department
import com.example.museumguide.data.MuseumModel
import com.example.museumguide.data.ObjectInfoModel
import com.example.museumguide.data.ObjectsModel
import com.example.museumguide.data.SearchResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MuseumsApi {

    @GET("/public/collection/v1/departments")
    fun getMuseums(): Call<ResponseMuseums>

    @GET("/public/collection/v1/objects")
    fun getObjectsFromMuseum(@Query("departmentIds") departmentIds: Int): Call<ObjectsModel>

    @GET("/public/collection/v1/objects/{objectID}")
    fun getInfoAboutObject(@Path("objectID") type: Int): Call<ObjectInfoModel>

    @GET("public/collection/v1/search")
    fun getSimilarObjects(
        @Query("q") query: String,
        @Query("artistOrCulture") artistOrCulture: Boolean = false,
        @Query("medium") medium: String? = null,
        @Query("geoLocation") geoLocation: String? = null,
    ): Call<SearchResponse>

    companion object {

        val retrofit by lazy {
            Retrofit
                .Builder()
                .baseUrl("https://collectionapi.metmuseum.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create<MuseumsApi>()
        }
    }

}

class ResponseMuseums(
    val departments: List<Department>,
)


