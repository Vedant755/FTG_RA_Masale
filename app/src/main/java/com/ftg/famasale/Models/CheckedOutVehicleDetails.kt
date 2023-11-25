package com.ftg.famasale.Models

data class CheckedOutVehicleDetails(
    val Employee: EmployeeDetails?,
    val Vehicle: VehicleDetails?,
    val vehicle_visit_check_in: Any?,
    val vehicle_visit_check_out: String?,
    val vehicle_visit_id: Int?
)