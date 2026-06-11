package com.myshelf.myshelf_app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.myshelf.myshelf_app.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var tokenManager: TokenManager? = null

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun init(context: Context) {
        if (tokenManager == null) {
            synchronized(this) {
                if (tokenManager == null) {
                    tokenManager = TokenManager(context.applicationContext)
                }
            }
        }
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = buildRetrofit(context.applicationContext)
                }
            }
        }
    }

    fun getTokenManager(context: Context): TokenManager {
        init(context)
        return tokenManager!!
    }

    fun makeApiService(context: Context): WardrobeApiService {
        init(context)
        return retrofit!!.create(WardrobeApiService::class.java)
    }

    fun makeApiService(): WardrobeApiService {
        val instance = retrofit
            ?: throw IllegalStateException(
                "RetrofitClient не инициализирован. Вызовите init(context) или makeApiService(context)."
            )
        return instance.create(WardrobeApiService::class.java)
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val manager = TokenManager(context)
        tokenManager = manager

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(manager))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
