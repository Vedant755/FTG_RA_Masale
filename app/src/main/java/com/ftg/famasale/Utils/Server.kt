package com.ftg.famasale.Utils

import com.ftg.famasale.Models.AllCheckOutVehiclesResponse
import com.ftg.famasale.Models.AllVehiclesResponse
import com.ftg.famasale.Models.AllVisitsOfEmployeesResponse
import com.ftg.famasale.Models.DepartmentId
import com.ftg.famasale.Models.EmployeeCheckInResponse
import com.ftg.famasale.Models.EmployeeCheckOutResponse
import com.ftg.famasale.Models.EmployeeId
import com.ftg.famasale.Models.EmployeeVisitId
import com.ftg.famasale.Models.GeneralResponse
import com.ftg.famasale.Models.GetEmployeesResponse
import com.ftg.famasale.Models.GetDaprtmentsResponse
import com.ftg.famasale.Models.LoginCred
import com.ftg.famasale.Models.LoginResponse
import com.ftg.famasale.Models.VehicleCheckOutId
import com.ftg.famasale.Models.VehicleCheckoutData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface Server {
    @POST("common/auth/login")
    fun login(
        @Body loginCred: LoginCred
    ): retrofit2.Call<LoginResponse>

    @GET("android/department")
    fun getDepartments(): retrofit2.Call<GetDaprtmentsResponse>

    @GET("android/employee")
    fun getAllEmployees(): retrofit2.Call<GetEmployeesResponse>

    @POST("android/employee/department/id")
    fun getEmployeesByDepartmentId(
        @Body departmentId: DepartmentId
    ): retrofit2.Call<GetEmployeesResponse>

    @POST("android/employee/visit/check-in")
    fun checkInEmployee(
        @Body employeeId: EmployeeId
    ): retrofit2.Call<EmployeeCheckInResponse>

    @PUT("android/employee/visit/check-out")
    fun checkOutEmployee(
        @Body employeeVisitId: EmployeeVisitId
    ): retrofit2.Call<EmployeeCheckOutResponse>

    @POST("android/employee/visit")
    fun getAllEmployeeVisits(): retrofit2.Call<AllVisitsOfEmployeesResponse>

    @GET("android/vehicle")
    fun getAllVehiclesList():retrofit2.Call<AllVehiclesResponse>  // 200

    @POST("android/vehicle/visit/check-out")  // 201
    fun checkOutVehicle(
        @Body data: VehicleCheckoutData
    ):retrofit2.Call<GeneralResponse>

    @PUT("android/vehicle/visit/check-in")  // 201
    fun checkInVehicle(
        @Body checkOutId: VehicleCheckOutId
    ): retrofit2.Call<GeneralResponse>

    @GET("android/vehicle/visit") // 200
    fun getAllCheckedOutVehicles(): retrofit2.Call<AllCheckOutVehiclesResponse>
}