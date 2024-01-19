package com.ftg.famasale.Models

data class AllTruckListResponse(
    val message: String?,
    val status: Int?,
    val success: Boolean?,
    val total: Int?,
    val data: List<TruckDetails>?
)
