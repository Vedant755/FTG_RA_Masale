package com.ftg.famasale.Models

data class AllVehiclesResponse(
    val data: List<VehicleDetails>?,
    val success: Boolean?,
    val total: Int?,
    val message: String?
)
