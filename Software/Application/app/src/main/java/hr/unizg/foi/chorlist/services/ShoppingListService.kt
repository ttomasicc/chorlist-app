package hr.unizg.foi.chorlist.services

import hr.unizg.foi.chorlist.models.requests.ShoppingListRequest
import hr.unizg.foi.chorlist.models.responses.ShoppingListItemsResponse
import hr.unizg.foi.chorlist.models.responses.ShoppingListResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

/**
 * Service responsible for CRUD operations with Shopping lists endpoint
 */
interface ShoppingListService {
    @Headers("Accept: application/json")
    @GET("shoppinglists")
    suspend fun getAll(
        @Header("Authorization") auth: String
    ): Response<List<ShoppingListResponse>>

    @Headers("Accept: application/json")
    @GET("shoppinglists/{id}")
    suspend fun get(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): Response<ShoppingListItemsResponse>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("shoppinglists")
    suspend fun add(
        @Header("Authorization") auth: String,
        @Body shoppingListRequest: ShoppingListRequest
    ): Response<ShoppingListResponse>

    @Headers("Content-Type: application/json")
    @PUT("shoppinglists/{id}")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body shoppingListRequest: ShoppingListRequest
    ): Response<Unit>

    @DELETE("shoppinglists/{id}")
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): Response<Unit>
}