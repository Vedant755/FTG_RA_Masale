package com.ftg.famasale.Models

data class VisitorCheckInResponse(
    val data: VisitorVisitDetails?,
    val message: String?,
    val status: Int?,
    val success: Boolean?
)