package com.ftg.famasale.Screens

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ftg.famasale.Adapters.VehicleCheckInAdapter
import com.ftg.famasale.Models.AllCheckOutVehiclesResponse
import com.ftg.famasale.Models.AllVehiclesResponse
import com.ftg.famasale.Models.CheckedOutVehicleDetails
import com.ftg.famasale.Models.EmployeeDetails
import com.ftg.famasale.Models.GeneralResponse
import com.ftg.famasale.Models.GetEmployeesResponse
import com.ftg.famasale.Models.VehicleCheckOutId
import com.ftg.famasale.Models.VehicleCheckoutData
import com.ftg.famasale.Models.VehicleDetails
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentVehiclesBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class VehiclesPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentVehiclesBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server
    private var allVehicleNames = ArrayList<String>()
    private var allEmployeesName = ArrayList<String>()
    private var allVehicles = ArrayList<VehicleDetails>()
    private var allEmployees = ArrayList<EmployeeDetails>()
    private var allCheckedOutVehicles = ArrayList<CheckedOutVehicleDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentVehiclesBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        bind.checkInRcv.layoutManager = LinearLayoutManager(requireContext())
        setActionListeners()

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        getAllCheckedOutVehicles(false)
        getAllEmployees()
        getAllVehicles()

        return bind.root
    }

    private fun getAllEmployees(){
        loadingDialog.startLoading()
        val response = server.getAllEmployees()
        response.enqueue(object: Callback<GetEmployeesResponse>{
            override fun onResponse(
                call: Call<GetEmployeesResponse>,
                response: Response<GetEmployeesResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty()) {
                        allEmployees = result as ArrayList<EmployeeDetails>
//                        allEmployeesName = result.map { "${it.employee_name} (${it.Department.department_name})" } as ArrayList<String>
                        allEmployeesName = result.map { it.employee_name ?: it.employee_number.toString() } as ArrayList<String>
                    }
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_text_list_item, allEmployeesName.toTypedArray())
                    bind.employeeName.setAdapter(adapter)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GetEmployeesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetEmployeesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllEmployees()
                } else if (call.isCanceled) {
                    getAllEmployees()
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun setActionListeners(){
        bind.showCheckOutForm.setOnClickListener {
            bind.showCheckOutForm.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showCheckOutForm.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showVehiclesToCheckIn.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showVehiclesToCheckIn.setTextColor(Color.parseColor("#616161"))

            bind.checkInRcv.visibility = View.GONE
            bind.checkOutLayout.visibility = View.VISIBLE
        }

        bind.showVehiclesToCheckIn.setOnClickListener {
            bind.showVehiclesToCheckIn.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showVehiclesToCheckIn.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showCheckOutForm.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showCheckOutForm.setTextColor(Color.parseColor("#616161"))

            bind.checkInRcv.visibility = View.VISIBLE
            bind.checkOutLayout.visibility = View.GONE
        }

        bind.checkOutButton.setOnClickListener {
            val employeeName = bind.employeeName.text.toString()
            val vehicleNumber = bind.vehicleNumber.text.toString()
            val reason = bind.reason.text.toString()

            if(!allEmployeesName.contains(employeeName)){
                Toast.makeText(requireContext(), "Select valid Employee", Toast.LENGTH_SHORT).show()
            }else if(!allVehicleNames.contains(vehicleNumber)){
                Toast.makeText(requireContext(), "Select valid vehicle", Toast.LENGTH_SHORT).show()
            }else if(reason.isBlank()){
                Toast.makeText(requireContext(), "Enter reason", Toast.LENGTH_SHORT).show()
            }else{
                checkout(employeeName, vehicleNumber, reason)
            }
        }
    }

    private fun getAllVehicles(){
        val response = server.getAllVehiclesList()
        response.enqueue(object: Callback<AllVehiclesResponse>{
            override fun onResponse(
                call: Call<AllVehiclesResponse>,
                response: Response<AllVehiclesResponse>
            ) {
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty()){
                        allVehicles = result as ArrayList<VehicleDetails>
                        allVehicleNames = result.map { it.vehicle_rc ?: it.vehicle_chassis.toString() } as ArrayList<String>
                    }
                    val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_text_list_item, allVehicleNames.toTypedArray())
                    bind.vehicleNumber.setAdapter(adapter)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), AllVehiclesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AllVehiclesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllVehicles()
                } else if (call.isCanceled) {
                    getAllVehicles()
                } else {
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun getAllCheckedOutVehicles(viewing: Boolean){
        if(viewing)
            loadingDialog.startLoading()

        allCheckedOutVehicles.clear()

        val response = server.getAllCheckedOutVehicles()
        response.enqueue(object: Callback<AllCheckOutVehiclesResponse>{
            override fun onResponse(
                call: Call<AllCheckOutVehiclesResponse>,
                response: Response<AllCheckOutVehiclesResponse>
            ) {
                if(viewing)
                    loadingDialog.stopLoading()

                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty()) {
                        allCheckedOutVehicles = result as ArrayList<CheckedOutVehicleDetails>
                    }
                }else if(response.code()==404){
                    //Nothing
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), AllVehiclesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }

                showCheckedOutVehicles()
            }

            override fun onFailure(call: Call<AllCheckOutVehiclesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllCheckedOutVehicles(viewing)
                } else if (call.isCanceled) {
                    getAllCheckedOutVehicles(viewing)
                } else {
                    if(viewing)
                        loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showCheckedOutVehicles() {
        bind.checkInRcv.adapter = VehicleCheckInAdapter(allCheckedOutVehicles){
            checkIn(it)
        }
    }

    private fun checkIn(vehicle: CheckedOutVehicleDetails) {
        loadingDialog.startLoading()

        val response = server.checkInVehicle(VehicleCheckOutId(vehicle.vehicle_visit_id ?: 0))
        response.enqueue(object: Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==201) {
                    Toast.makeText(requireContext(), "Vehicle checked-in successfully", Toast.LENGTH_SHORT).show()
                    getAllCheckedOutVehicles(true)
                }
                else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkIn(vehicle)
                } else if (call.isCanceled) {
                    checkIn(vehicle)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun checkout(name: String, vehicle: String, reason: String){
        loadingDialog.startLoading()

        var vehicleId = 0
        var employeeId = 0

        try{
            vehicleId = allVehicles.filter { it.vehicle_rc?.contains(vehicle, true) == true || it.vehicle_chassis?.contains(vehicle, true) == true }[0].vehicle_id ?: 0
            employeeId = allEmployees.filter { it.employee_name?.contains(name, true) == true || it.employee_number?.toString()?.contains(name, true) == true }[0].employee_id ?: 0
        }catch (e: Exception){
            Log.d("XYZXYZ", e.localizedMessage?.toString() ?: "")
        }

        Log.d("XYZXYZ", "Name: $employeeId, vehicle: $vehicleId, reason: $reason")
        val response = server.checkOutVehicle(VehicleCheckoutData(employeeId, vehicleId, reason))
        response.enqueue(object: Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==201){
                    getAllCheckedOutVehicles(false)

                    bind.employeeName.setText("")
                    bind.vehicleNumber.setText("")
                    bind.reason.setText("")

                    Toast.makeText(requireContext(), "Vehicle checked-out successfully", Toast.LENGTH_SHORT).show()
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkout(name, vehicle, reason)
                } else if (call.isCanceled) {
                    checkout(name, vehicle, reason)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
}