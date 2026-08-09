package com.example.data.model

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object IstanbulLocationData {
    val districts = listOf(
        "Bayrampaşa",
        "Esenler",
        "Gaziosmanpaşa",
        "Zeytinburnu",
        "Fatih",
        "Eyüpsultan"
    )

    // sancakkombi-web/lib/istanbul-mahalleleri.ts ile senkron tutulan resmi mahalle isimleri.
    private val neighborhoodsByDistrict: Map<String, List<String>> = mapOf(
        "Bayrampaşa" to listOf(
            "Altıntepsi", "Cevatpaşa", "İsmetpaşa", "Kartaltepe", "Kocatepe",
            "Muratpaşa", "Orta", "Terazidere", "Vatan", "Yenidoğan", "Yıldırım"
        ),
        "Esenler" to listOf(
            "Atışalanı", "Birlik", "Davutpaşa", "Fatih", "Havaalanı",
            "Kazım Karabekir", "Menderes", "Oruçreis", "Tuna", "Yavuz Selim"
        ),
        "Gaziosmanpaşa" to listOf(
            "Bağlarbaşı", "Barbaros Hayrettin Paşa", "Karlıtepe", "Karayolları",
            "Merkez", "Mevlana", "Sarıgöl", "Yıldıztabya"
        ),
        "Zeytinburnu" to listOf(
            "Beştelsiz", "Gökalp", "Kazlıçeşme", "Maltepe", "Merkezefendi",
            "Seyitnizam", "Sümer", "Telsiz", "Veliefendi", "Yeşiltepe"
        ),
        "Fatih" to listOf(
            "Aksaray", "Balat", "Çarşamba", "Cibali", "Fener", "Haseki",
            "Karagümrük", "Laleli", "Sultanahmet", "Vefa", "Zeyrek"
        ),
        "Eyüpsultan" to listOf(
            "Akşemsettin", "Alibeyköy", "Defterdar", "Göktürk", "Güzeltepe",
            "Kemerburgaz", "Nişancı", "Rami", "Silahtarağa", "Yeşilpınar"
        )
    )

    private val fallbackStreets = listOf(
        "Atatürk Caddesi No:12",
        "Cami Sokak No:5",
        "Millet Caddesi No:18",
        "Cumhuriyet Caddesi No:24",
        "Fatih Caddesi No:8"
    )

    // İçinde sadece asıl hizmet bölgesi olan Bayrampaşa/Esenler/Gaziosmanpaşa için gerçek
    // sokak verisi bulunur (assets/istanbul_sokaklari_mahalle.json, web ile senkron).
    // Diğer ilçelerde eşleşme yoksa fallbackStreets'e düşülür.
    @Volatile
    private var cachedRawJson: String? = null

    @Volatile
    private var streetsByDistrictMahalleCache: Map<String, Map<String, List<String>>>? = null

    private fun getParsedData(context: Context): Map<String, Map<String, List<String>>> {
        streetsByDistrictMahalleCache?.let { return it }

        synchronized(this) {
            streetsByDistrictMahalleCache?.let { return it }

            val json = cachedRawJson ?: try {
                context.applicationContext.assets
                    .open("istanbul_sokaklari_mahalle.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                    .also { cachedRawJson = it }
            } catch (e: Exception) {
                ""
            }

            if (json.isBlank()) {
                val emptyMap = emptyMap<String, Map<String, List<String>>>()
                streetsByDistrictMahalleCache = emptyMap
                return emptyMap
            }

            return try {
                val type = Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        Types.newParameterizedType(List::class.java, String::class.java)
                    )
                )
                val adapter = Moshi.Builder().build().adapter<Map<String, Map<String, List<String>>>>(type)
                val parsed = adapter.fromJson(json) ?: emptyMap()
                streetsByDistrictMahalleCache = parsed
                parsed
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    fun getNeighborhoods(district: String): List<String> {
        val plainNames = neighborhoodsByDistrict[district] ?: listOf(
            "Merkez", "Fatih", "Cumhuriyet", "Yenidoğan", "Atatürk"
        )
        return plainNames.map { "$it Mah." }
    }

    fun getStreets(context: Context, district: String, neighborhood: String): List<String> {
        val plainNeighborhood = neighborhood.removeSuffix(" Mah.").trim()
        val streets = getParsedData(context)[district]?.get(plainNeighborhood)
        return if (!streets.isNullOrEmpty()) streets else fallbackStreets
    }
}
