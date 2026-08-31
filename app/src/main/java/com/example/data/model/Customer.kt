package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val district: String = "",
    val address: String = "",
    val appointmentCount: Int = 0,
    val activeAppointmentCount: Int = 0,
    val notes: String = "",
    val isArchived: Boolean = false
)
