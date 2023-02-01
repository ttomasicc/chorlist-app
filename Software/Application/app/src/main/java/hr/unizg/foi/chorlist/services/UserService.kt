package hr.unizg.foi.chorlist.services

import hr.unizg.foi.chorlist.models.requests.UserLoginRequest
import hr.unizg.foi.chorlist.models.requests.UserRegisterRequest
import hr.unizg.foi.chorlist.models.requests.UserUpdateRequest
import hr.unizg.foi.chorlist.models.responses.UserJwtResponse
import hr.unizg.foi.chorlist.models.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Service responsible for CRUD operations with Users endpoint
 */
interface UserService {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("users/login")
    suspend fun login(
        @Body userLoginRequest: UserLoginRequest
    ): Response<Unit>

    @GET("users/logout")
    suspend fun logout(): Response<Unit>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("users/register")
    suspend fun register(
        @Body userRegisterRequest: UserRegisterRequest
    ): Response<Unit>

    @Headers("Accept: application/json")
    @GET("users/jwt")
    suspend fun getJWT(): Response<UserJwtResponse>

    @Headers(
        "Content-Type: application/json"
    )
    @PUT("users/update")
    suspend fun updateUser(
        @Body userUpdateRequest: UserUpdateRequest,
        @Header("Authorization") auth: String
    ): Response<Unit>

    @Headers("Content-Type: application/json")
    @GET("users/current")
    suspend fun getUser(@Header("Authorization") auth: String): Response<UserResponse>
}