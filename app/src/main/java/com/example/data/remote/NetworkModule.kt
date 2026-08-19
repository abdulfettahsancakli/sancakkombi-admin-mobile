package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import com.example.data.model.AppointmentStatus
import com.example.data.model.FinanceType
import com.example.data.model.ProposalStatus
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class AppointmentStatusAdapter {
    @ToJson
    fun toJson(status: AppointmentStatus): String = status.name

    @FromJson
    fun fromJson(status: String?): AppointmentStatus = when (status?.uppercase()?.trim()) {
        "BEKLIYOR", "PENDING" -> AppointmentStatus.BEKLIYOR
        "ONAYLANDI", "CONFIRMED" -> AppointmentStatus.ONAYLANDI
        "TAMAMLANDI", "COMPLETED" -> AppointmentStatus.TAMAMLANDI
        "IPTAL", "CANCELLED" -> AppointmentStatus.IPTAL
        else -> AppointmentStatus.ONAYLANDI
    }
}

class FinanceTypeAdapter {
    @ToJson
    fun toJson(type: FinanceType): String = type.name

    @FromJson
    fun fromJson(type: String?): FinanceType = when (type?.uppercase()?.trim()) {
        "GELIR", "INCOME" -> FinanceType.GELIR
        "GIDER", "EXPENSE" -> FinanceType.GIDER
        else -> FinanceType.GELIR
    }
}

class ProposalStatusAdapter {
    @ToJson
    fun toJson(status: ProposalStatus): String = status.name

    @FromJson
    fun fromJson(status: String?): ProposalStatus = when (status?.uppercase()?.trim()) {
        "PENDING", "BEKLIYOR", "DRAFT" -> ProposalStatus.PENDING
        "APPROVED", "ONAYLANDI", "ACCEPTED" -> ProposalStatus.APPROVED
        "REJECTED", "REDDEDILDI" -> ProposalStatus.REJECTED
        else -> ProposalStatus.PENDING
    }
}

object NetworkModule {
    private val moshi: Moshi = Moshi.Builder()
        .add(AppointmentStatusAdapter())
        .add(FinanceTypeAdapter())
        .add(ProposalStatusAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    val adminApiService: AdminApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AdminApiService::class.java)
    }
}
