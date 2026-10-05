package com.ankit.assessmentankit.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ankit.assessmentankit.data.local.AppDatabase
import com.ankit.assessmentankit.data.remote.DummyJsonApi
import com.ankit.assessmentankit.data.repository.CartRepository
import com.ankit.assessmentankit.data.repository.ProductRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val dummyJsonApi: DummyJsonApi by lazy {
        retrofit.create(DummyJsonApi::class.java)
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(
            api = dummyJsonApi,
            productDao = database.productDao(),
            gson = gson
        )
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(
            cartDao = database.cartDao(),
            gson = gson
        )
    }

    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(context)
    }
}
