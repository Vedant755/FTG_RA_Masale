package com.ftg.famasale.Models

data class TruckDetails(
    val createdAt: String?,
    val driver_gender: String?,
    val driver_mobile: String?,
    val driver_name: String?,
    val truck_check_in_added_by: Int?,
    val truck_check_in_added_by_id: String?,
    val truck_description: String?,
    val truck_id: Int?,
    val truck_number: String?,
    val truck_status: String?,
    val quantity: String?,              // Only for RawMaterial Truck
    val updatedAt: String?
)