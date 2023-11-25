package com.ftg.famasale.Models

data class LoginResponse(
    val authority: List<String>?,
    val data: LoggedInUserDetails?,
    val success: Boolean?,
    val token: String?,
    val message: String?
)