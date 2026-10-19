package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Dns
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException

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
        "EXPIRED", "SURESI_DOLDU" -> ProposalStatus.EXPIRED
        else -> ProposalStatus.PENDING
    }
}

object NetworkModule {
    /**
     * Some mobile/private DNS providers intermittently fail to resolve the www
     * record. Keep normal system DNS first, and only fall back for our own API
     * host. TLS still validates www.sancakkombi.com through HTTPS/SNI.
     */
    private val resilientDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (error: UnknownHostException) {
                if (hostname.equals("www.sancakkombi.com", ignoreCase = true) ||
                    hostname.equals("sancakkombi.com", ignoreCase = true)
                ) {
                    listOf(
                        InetAddress.getByAddress(hostname, byteArrayOf(216.toByte(), 198.toByte(), 79, 1)),
                        InetAddress.getByAddress(hostname, byteArrayOf(64, 29, 17, 1))
                    )
                } else {
                    throw error
                }
            }
        }
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(AppointmentStatusAdapter())
        .add(FinanceTypeAdapter())
        .add(ProposalStatusAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun okHttpClient(tokenStore: TokenStore): OkHttpClient {
        return OkHttpClient.Builder()
            .dns(resilientDns)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // Do not write passwords, bearer tokens, or uploaded file contents to Logcat.
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
                }
            )
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 401) {
                    Log.w("NetworkModule", "Admin API session is invalid or expired; clearing local session.")
                    runBlocking { tokenStore.clearToken() }
                }
                response
            }
            .build()
    }

    fun createAdminApiService(tokenStore: TokenStore): AdminApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient(tokenStore))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AdminApiService::class.java)
    }
}
