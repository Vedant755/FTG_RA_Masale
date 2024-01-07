package com.ftg.famasale.Models

data class TruckRegisterResponse(
    val message: String?,
    val status: Int?,
    val success: Boolean?,
    val data: TruckDetails?
)