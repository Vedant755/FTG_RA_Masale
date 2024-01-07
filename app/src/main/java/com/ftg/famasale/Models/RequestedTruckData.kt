package com.ftg.famasale.Models

data class RequestedTruckData(
    val driver_gender: String?,
    val driver_mobile: String?,
    val driver_name: String?,
    val truck_description: String?,
    val truck_number: String?,
    val quantity: String?           //Only for RawMaterial Truck
)