package com.ftg.famasale.Models

data class AllCheckOutVehiclesResponse(
    val message: String?,
    val status: Int?,
    val success: Boolean?,
    val data: List<CheckedOutVehicleDetails>?
)
