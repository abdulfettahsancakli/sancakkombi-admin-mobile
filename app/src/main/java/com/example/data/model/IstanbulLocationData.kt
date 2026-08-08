package com.example.data.model

object IstanbulLocationData {
    val districts = listOf(
        "Bayrampaşa",
        "Esenler",
        "Gaziosmanpaşa",
        "Zeytinburnu",
        "Fatih",
        "Eyüpsultan"
    )

    // Map<DistrictName, Map<NeighborhoodName, List<StreetName>>>
    val locationData: Map<String, Map<String, List<String>>> = mapOf(
        "Bayrampaşa" to mapOf(
            "Yıldırım Mah." to listOf("Ali Fuat Başgil Caddesi", "Şehit Kamil Caddesi", "Eski Edirne Asfaltı", "Park Sokak", "Millet Caddesi", "Muhtar Sokak", "Çiçek Sokak"),
            "Muratpaşa Mah." to listOf("Kamil Caddesi", "Kosova Sokak", "Uluyol Caddesi", "Fabrikalar Sokak", "Tuna Caddesi", "Eski Edirne Asfaltı"),
            "Yenidoğan Mah." to listOf("Abdi İpekçi Caddesi", "Demirkapı Caddesi", "Numunebağ Caddesi", "Cami Sokak", "Pazaryeri Sokak"),
            "Kartaltepe Mah." to listOf("50. Yıl Caddesi", "Bilgehan Caddesi", "Fatih Caddesi", "Yağmur Sokak", "Ankara Caddesi"),
            "Altıntepsi Mah." to listOf("Uygur Caddesi", "Filiz Sokak", "Akpınar Sokak", "Ferhatpaşa Caddesi", "Şehit Kamil Sokak"),
            "İsmetpaşa Mah." to listOf("Kenan Evren Caddesi", "Tuna Caddesi", "İsmetpaşa Caddesi", "Engin Sokak", "Çevreyolu Sokak"),
            "Kocatepe Mah." to listOf("Mega Center İçi", "19. Sokak", "Gümrük Yolu", "50. Yıl Caddesi", "Avasköy Yolu"),
            "Vatan Mah." to listOf("Uygur Caddesi", "Cami Sokak", "Esenler Caddesi", "Bağlar Caddesi"),
            "Orta Mah." to listOf("Demirkapı Caddesi", "Maltepe Caddesi", "Çevreyolu Sokak", "Kuşak Sokak"),
            "Terazidere Mah." to listOf("60. Yıl Caddesi", "Gürsoy Sokak", "Eski Gençosman Caddesi"),
            "Cevatpaşa Mah." to listOf("Millet Caddesi", "Rami Cuma Caddesi", "Barbaros Caddesi")
        ),
        "Esenler" to mapOf(
            "Menderes Mah." to listOf("35. Sokak", "Atışalanı Caddesi", "30. Sokak", "Ömer Seyfettin Caddesi", "Gazi Caddesi"),
            "Nine Hatun Mah." to listOf("108. Sokak", "İnönü Caddesi", "115. Sokak", "Aziziye Caddesi", "Davutpaşa Caddesi"),
            "Fatih Mah." to listOf("220. Sokak", "Çinçindere Caddesi", "235. Sokak", "Üçyüzlü Caddesi", "Eski Londra Asfaltı"),
            "Turgut Reis Mah." to listOf("Cengiz Topel Caddesi", "480. Sokak", "Karaosmanoğlu Caddesi", "510. Sokak"),
            "Kemer Mah." to listOf("Atışalanı Caddesi", "905. Sokak", "Malazgirt Caddesi", "Kemer Caddesi"),
            "Birlik Mah." to listOf("800. Sokak", "Atışalanı Caddesi", "815. Sokak", "Mehmet Akif Caddesi"),
            "Havaalanı Mah." to listOf("Taşocağı Caddesi", "Mehmet Akif İnan Caddesi", "1056. Sokak", "Güzelyurt Caddesi"),
            "Davutpaşa Mah." to listOf("Davutpaşa Caddesi", "60. Sokak", "Yıldız Caddesi", "Cami Sokak"),
            "Oruçreis Mah." to listOf("Barbaros Caddesi", "Giyimkent Caddesi", "Tekstilkent Yolu"),
            "Tuna Mah." to listOf("Mahmutbey Caddesi", "720. Sokak", "Tuna Caddesi")
        ),
        "Gaziosmanpaşa" to mapOf(
            "Merkez Mah." to listOf("Salihpaşa Caddesi", "Madalyon Sokak", "Bağlarbaşı Caddesi", "Laleli Sokak", "Ordu Caddesi", "Meydan Sokak"),
            "Barbaros Hayrettin Paşa Mah." to listOf("1008. Sokak", "1020. Sokak", "Mimar Sinan Caddesi", "Şehit Mustafa Yeşil Caddesi", "1050. Sokak"),
            "Karadeniz Mah." to listOf("Menderes Caddesi", "1120. Sokak", "Mehmet Akif Caddesi", "Cebeci Caddesi", "1150. Sokak"),
            "Fevzi Çakmak Mah." to listOf("Fevzi Çakmak Caddesi", "850. Sokak", "Cengiz Topel Caddesi", "Kemal Şahin Sokak", "870. Sokak"),
            "Mevlana Mah." to listOf("İbrahim Hayırlıoğlu Caddesi", "860. Sokak", "875. Sokak", "Hekim Suyu Caddesi", "Mevlana Caddesi"),
            "Kazım Karabekir Mah." to listOf("820. Sokak", "Abdi İpekçi Caddesi", "835. Sokak", "Ordu Caddesi"),
            "Yıldıztabya Mah." to listOf("Yıldıztabya Caddesi", "Taşlıtarla Sokak", "Kırkıl Sokak", "Sert Sokak"),
            "Pazariçi Mah." to listOf("Ordu Caddesi", "Pazariçi Meydan Sokak", "Çayır Sokak", "Çamlık Sokak"),
            "Şemsipaşa Mah." to listOf("50. Yıl Caddesi", "Cengiz Topel Caddesi", "Küçükköy Yolu"),
            "Yenidoğan Mah." to listOf("Ordu Caddesi", "Taşlıtarla Sokak", "Laleli Sokak")
        ),
        "Zeytinburnu" to mapOf(
            "Telsiz Mah." to listOf("Balıklı Kazlıçeşme Yolu", "85. Sokak", "Gül Cami Sokak", "İnönü Caddesi"),
            "Beştelsiz Mah." to listOf("101. Sokak", "Rauf Denktaş Caddesi", "Bulvar Caddesi", "Semih Erden Caddesi"),
            "Gökalp Mah." to listOf("48. Sokak", "Zeytinburnu Bulvarı", "54. Sokak", "Merve Caddesi"),
            "Seyitnizam Mah." to listOf("Mevlana Caddesi", "Seyitnizam Caddesi", "Demirciler Sitesi Yol 1"),
            "Kazlıçeşme Mah." to listOf("Abay Kunanbay Caddesi", "Kennedy Caddesi", "Marmaray Caddesi")
        ),
        "Fatih" to mapOf(
            "Aksaray Mah." to listOf("Namık Kemal Caddesi", "Cerrahpaşa Caddesi", "Valide Cami Sokak", "Vatan Caddesi"),
            "Karagümrük Mah." to listOf("Fevzipaşa Caddesi", "Saraymeydanı Sokak", "Sofalı Çeşme Caddesi"),
            "Mevlanakapı Mah." to listOf("Mevlevihane Caddesi", "Başvekil Caddesi", "100. Yıl Caddesi")
        ),
        "Eyüpsultan" to mapOf(
            "Alibeyköy Mah." to listOf("Atatürk Caddesi", "Vardar Caddesi", "Namık Kemal Caddesi", "Silahtarağa Caddesi"),
            "Göktürk Mah." to listOf("İstanbul Caddesi", "Teleferik Sokak", "Arcadium Çarşısı Sokak"),
            "Nişancı Mah." to listOf("Eyüpsultan Bulvarı", "Davutpaşa Caddesi", "Savaklar Caddesi")
        )
    )

    fun getNeighborhoods(district: String): List<String> {
        return locationData[district]?.keys?.toList() ?: listOf(
            "Merkez Mah.",
            "Fatih Mah.",
            "Cumhuriyet Mah.",
            "Yenidoğan Mah.",
            "Atatürk Mah."
        )
    }

    fun getStreets(district: String, neighborhood: String): List<String> {
        val districtData = locationData[district]
        if (districtData != null) {
            val streets = districtData[neighborhood]
            if (streets != null && streets.isNotEmpty()) {
                return streets
            }
        }
        return listOf(
            "Atatürk Caddesi No:12",
            "Cami Sokak No:5",
            "Millet Caddesi No:18",
            "Cumhuriyet Caddesi No:24",
            "Fatih Caddesi No:8"
        )
    }
}
