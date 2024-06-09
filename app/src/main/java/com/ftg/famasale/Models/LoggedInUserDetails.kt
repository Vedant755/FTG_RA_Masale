package com.ftg.famasale.Models

data class LoggedInUserDetails(
    val createdAt: String?,
    val guard_added_by: String?,
    val guard_added_by_id: Int?,
    val guard_address: String?,
    val guard_deleted: Boolean?,
    val guard_email: String?,
    val guard_gender: String?,
    val guard_id: Int?,
    val guard_mobile: Long?,
    val guard_name: String?,
    val guard_status: Boolean?,
    val guard_username: String?,
    val updatedAt: String?,
    val department_name: String
)