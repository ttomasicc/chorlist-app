package hr.unizg.foi.chorlist.interceptors

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Intercepts all application and modifies the Authorization header to accompany the Chorlist REST
 * API format.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        original.header("Authorization")?.let { token ->
            val requestBuilder = original
                .newBuilder()
                .method(original.method, original.body)
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $token")

            return chain.proceed(requestBuilder.build())
        }

        return chain.proceed(original)
    }
}