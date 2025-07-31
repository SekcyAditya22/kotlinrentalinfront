package com.example.rentalinn.network

import android.content.Context
import com.example.rentalinn.utils.DataStoreManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor(context: Context) {
    private val dataStoreManager = DataStoreManager.getInstance(context)

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking {
            dataStoreManager.token.first()
        }

        val request = chain.request().newBuilder().apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        val response = chain.proceed(request)

        // Handle token expiration
        if (response.code == 401) {
            // Clear expired token
            runBlocking {
                dataStoreManager.clearData()
            }
        }

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val rentalApiService: RentalApiService = retrofit.create(RentalApiService::class.java)
    val chatApiService: ChatApiService = retrofit.create(ChatApiService::class.java)

    fun <T> create(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    companion object {
        private const val BASE_URL = "https://beexpress.peachy.icu/api/"
        
        @Volatile
        private var instance: RetrofitClient? = null

        fun getInstance(context: Context): RetrofitClient {
            return instance ?: synchronized(this) {
                instance ?: RetrofitClient(context.applicationContext).also { instance = it }
            }
        }
    }
} 