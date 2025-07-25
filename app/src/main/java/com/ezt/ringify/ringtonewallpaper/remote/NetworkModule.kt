package com.ezt.ringify.ringtonewallpaper.remote

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ezt.ringify.ringtonewallpaper.remote.api.ApiService
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val token = RingtoneRepository.TOKEN
        val interceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("AuthorizationApi", "$token")
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://ringtone-2.cdtgames.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    private fun generateAccessToken(): String {
        val secret = "abcadhjgashjd1231" // Secret key
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withClaim("data", mapOf("client_id" to "abc", "type" to 1))
            .sign(algorithm)
    }
}