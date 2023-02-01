package hr.unizg.foi.chorlist.services

import hr.unizg.foi.chorlist.models.requests.ItemRequest
import hr.unizg.foi.chorlist.models.responses.ItemResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Service responsible for CRUD operations with Items endpoint
 */
interface ItemService {
    @Headers("Accept: application/json")
    @GET("items")
    suspend fun search(
        @Header("Authorization") auth: String,
        @Query("query") query: String
    ): Response<List<ItemResponse>>

    @Headers("Content-Type: application/json")
    @POST("items")
    suspend fun add(
        @Header("Authorization") auth: String,
        @Body item: ItemRequest
    ): Response<ItemResponse>

    @Headers("Content-Type: application/json")
    @PUT("items/{id}")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body item: ItemRequest
    ): Response<Unit>

    @DELETE("items/{id}")
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): Response<Unit>
}