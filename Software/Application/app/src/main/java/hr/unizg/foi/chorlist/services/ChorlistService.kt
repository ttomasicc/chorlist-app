package hr.unizg.foi.chorlist.services

import com.google.gson.GsonBuilder
import hr.unizg.foi.chorlist.BASE_URL
import hr.unizg.foi.chorlist.interceptors.AuthInterceptor
import hr.unizg.foi.chorlist.models.responses.ErrorResponse
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.time.LocalDateTime

/**
 * Main service used. It connects to the locally served REST API that manages Chorlist data.
 * It provides sub-services for all application-required endpoints.
 */
object ChorlistService {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .create()
    private val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .cookieJar(JavaNetCookieJar(CookieManager()))
                .build()
        )
        .build()

    val userService: UserService = instance.create(UserService::class.java)
    val shoppingListService = instance.create(ShoppingListService::class.java)
    val itemService = instance.create(ItemService::class.java)

    val errorConverter: Converter<ResponseBody, ErrorResponse> = instance
        .responseBodyConverter(ErrorResponse::class.java, arrayOfNulls<Annotation>(0))
}