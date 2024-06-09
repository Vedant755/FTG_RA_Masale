package com.ftg.famasale.Screens

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ftg.famasale.Adapters.EmployeesCheckInAdapter
import com.ftg.famasale.Adapters.EmployeesCheckOutAdapter
import com.ftg.famasale.Models.AllVisitsOfEmployeesResponse
import com.ftg.famasale.Models.EmployeeCheckInResponse
import com.ftg.famasale.Models.EmployeeCheckOutResponse
import com.ftg.famasale.Models.EmployeeDetails
import com.ftg.famasale.Models.EmployeeId
import com.ftg.famasale.Models.EmployeeVisitId
import com.ftg.famasale.Models.GetEmployeesResponse
import com.ftg.famasale.Models.EmployeeVisitDetails
import com.ftg.famasale.Utils.Constant.BASE_URL
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentEmployeesPageBinding
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
class EmployeesPage : Fragment() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentEmployeesPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server

    private var allEmployees = ArrayList<EmployeeDetails>()
    private var allCheckedIns = ArrayList<EmployeeVisitDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentEmployeesPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        bind.rcv.layoutManager = LinearLayoutManager(requireContext())
        setActionListeners()

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(Server::class.java)

        getAllEmployees(true)
        getAllCheckedIns(false)
        return bind.root
    }

    private fun setActionListeners(){
        bind.showEmployees.setOnClickListener {
            bind.showEmployees.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showEmployees.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showCheckIns.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showCheckIns.setTextColor(Color.parseColor("#616161"))

            showEmployees()
        }

        bind.showCheckIns.setOnClickListener {
            bind.showCheckIns.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showCheckIns.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showEmployees.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showEmployees.setTextColor(Color.parseColor("#616161"))

            showCheckIns()
        }

    }

    private fun getAllCheckedIns(viewing: Boolean){
        allCheckedIns.clear()

        if(viewing)
            loadingDialog.startLoading()

        val response = server.getAllEmployeeVisits()
        response.enqueue(object: Callback<AllVisitsOfEmployeesResponse>{
            override fun onResponse(
                call: Call<AllVisitsOfEmployeesResponse>,
                response: Response<AllVisitsOfEmployeesResponse>
            ) {
                if(viewing)
                    loadingDialog.stopLoading()

                if(response.code() == 200 && !response.body()?.data.isNullOrEmpty()){
                    val result = response.body()?.data as ArrayList<EmployeeVisitDetails>?
                    if(!result.isNullOrEmpty())
                        allCheckedIns = result

                    if(viewing)
                        showCheckIns()

                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), AllVisitsOfEmployeesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                    if(viewing)
                        showCheckIns()
                }
            }

            override fun onFailure(call: Call<AllVisitsOfEmployeesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllCheckedIns(viewing)
                } else if (call.isCanceled) {
                    getAllCheckedIns(viewing)
                } else {
                    if(viewing)
                        loadingDialog.stopLoading()

                    Toast.makeText(requireContext(), "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getAllEmployees(viewing: Boolean){
        allEmployees.clear()

        if(viewing)
            loadingDialog.startLoading()

        val response = server.getAllEmployees()
        response.enqueue(object : Callback<GetEmployeesResponse>{
            override fun onResponse(
                call: Call<GetEmployeesResponse>,
                response: Response<GetEmployeesResponse>
            ) {
                if(viewing)
                    loadingDialog.stopLoading()

                if(response.code() == 200){
                    val result = response.body()?.data as ArrayList<EmployeeDetails>?
                    if(!result.isNullOrEmpty())
                        allEmployees = result

                    if(viewing)
                        showEmployees()
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GetEmployeesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                    if(viewing)
                        showEmployees()
                }
            }

            override fun onFailure(call: Call<GetEmployeesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllEmployees(viewing)
                } else if (call.isCanceled) {
                    getAllEmployees(viewing)
                } else {
                    if(viewing)
                        loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun showEmployees(){
        val filteredList = allEmployees.filter { it.employee_check_in_status != true }

        bind.rcv.adapter = EmployeesCheckInAdapter(filteredList){
            checkIn(it)
        }
    }

    private fun showCheckIns(){
        bind.rcv.adapter = EmployeesCheckOutAdapter(allCheckedIns){
            checkOut(it)
        }
    }

    private fun checkIn(employee: EmployeeDetails){
        loadingDialog.startLoading()
        val response = server.checkInEmployee(EmployeeId(employee.employee_id))
        response.enqueue(object: Callback<EmployeeCheckInResponse>{
            override fun onResponse(
                call: Call<EmployeeCheckInResponse>,
                response: Response<EmployeeCheckInResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code() == 201){
                    Toast.makeText(requireContext(), "Employee Checked-In successfully ", Toast.LENGTH_SHORT).show()
                    getAllEmployees(true)
                    getAllCheckedIns(false)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), EmployeeCheckInResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<EmployeeCheckInResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkIn(employee)
                } else if (call.isCanceled) {
                    checkIn(employee)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun checkOut(visit: EmployeeVisitDetails){
        loadingDialog.startLoading()
        val response = server.checkOutEmployee(EmployeeVisitId(visit.employee_visit_id))
        response.enqueue(object: Callback<EmployeeCheckOutResponse>{
            override fun onResponse(
                call: Call<EmployeeCheckOutResponse>,
                response: Response<EmployeeCheckOutResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code() == 201){
                    Toast.makeText(requireContext(), "Employee Checked-Out successfully ", Toast.LENGTH_SHORT).show()
                    getAllCheckedIns(true)
                    getAllEmployees(false)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), EmployeeCheckOutResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<EmployeeCheckOutResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkOut(visit)
                } else if (call.isCanceled) {
                    checkOut(visit)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}