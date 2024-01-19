package com.ftg.famasale.Utils

import com.ftg.famasale.Models.AllCheckOutVehiclesResponse
import com.ftg.famasale.Models.AllTruckListResponse
import com.ftg.famasale.Models.AllVehiclesResponse
import com.ftg.famasale.Models.AllVisitsOfEmployeesResponse
import com.ftg.famasale.Models.DepartmentId
import com.ftg.famasale.Models.TruckRegisterResponse
import com.ftg.famasale.Models.EmployeeCheckInResponse
import com.ftg.famasale.Models.EmployeeCheckOutResponse
import com.ftg.famasale.Models.EmployeeId
import com.ftg.famasale.Models.EmployeeVisitId
import com.ftg.famasale.Models.GeneralResponse
import com.ftg.famasale.Models.GetAllVacanciesResponse
import com.ftg.famasale.Models.GetAllVisitorVisitsResponse
import com.ftg.famasale.Models.GetEmployeesResponse
import com.ftg.famasale.Models.GetDaprtmentsResponse
import com.ftg.famasale.Models.LoginCred
import com.ftg.famasale.Models.LoginResponse
import com.ftg.famasale.Models.RegisterCandidateResponse
import com.ftg.famasale.Models.RequestedTruckData
import com.ftg.famasale.Models.VehicleCheckOutId
import com.ftg.famasale.Models.VehicleCheckoutData
import com.ftg.famasale.Models.VisitorCheckInResponse
import com.ftg.famasale.Models.VisitorId
import com.ftg.famasale.Models.VisitorVisitData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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

    @GET("android/visitor")  // 200
    fun getAllVisitors(): retrofit2.Call<GetAllVisitorVisitsResponse>

    @POST("android/visitor/check-in") // 200
    fun visitorCheckIn(
        @Body data: VisitorVisitData
    ): retrofit2.Call<VisitorCheckInResponse>

    @PUT("android/visitor/check-out")
    fun visitorCheckOut(
        @Body visitorId: VisitorId
    ): retrofit2.Call<GeneralResponse>

    @PUT("android/visitor/cancel")  // 200
    fun cancelVisitorVisit(
        @Body visitorId: VisitorId
    ): retrofit2.Call<GeneralResponse>

    @GET("android/job")  // 200
    fun getAllVacancies(): retrofit2.Call<GetAllVacanciesResponse>

    @Multipart
    @POST("android/candidate/register")  // 201
    fun registerCandidateForJob(
        @Part imageFile:MultipartBody.Part,
        @Part("candidate_name") candidateName :RequestBody,
        @Part("candidate_address") candidateAddress :RequestBody,
        @Part("candidate_email") candidateEmail :RequestBody,
        @Part("candidate_mobile") candidateMobile :RequestBody,
        @Part("candidate_gender") candidateGender :RequestBody,
        @Part("candidate_qualification") candidateQualification :RequestBody,
        @Part("job_id") jobId :RequestBody,
    ): retrofit2.Call<RegisterCandidateResponse>

    @POST("android/dispatch/truck/register")   // 200
    fun registerDispatchTruck(
        @Body truckDetails: RequestedTruckData
    ): retrofit2.Call<TruckRegisterResponse>

    @POST("android/raw/truck/register")        // 200
    fun registerRawMaterialTruck(
        @Body truckDetails: RequestedTruckData
    ): retrofit2.Call<TruckRegisterResponse>

    @GET("android/raw/truck")                  // 200
    fun getAllRawMaterialTrucks():retrofit2.Call<AllTruckListResponse>

    @GET("android/dispatch/truck")             // 200
    fun getAllDispatchTrucks():retrofit2.Call<AllTruckListResponse>
}