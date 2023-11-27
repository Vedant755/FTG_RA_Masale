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
import com.ftg.famasale.Adapters.VisitorVisitsAdapter
import com.ftg.famasale.Models.EmployeeDetails
import com.ftg.famasale.Models.GeneralResponse
import com.ftg.famasale.Models.GetAllVisitorVisitsResponse
import com.ftg.famasale.Models.GetEmployeesResponse
import com.ftg.famasale.Models.VisitorCheckInResponse
import com.ftg.famasale.Models.VisitorId
import com.ftg.famasale.Models.VisitorVisitData
import com.ftg.famasale.Models.VisitorVisitDetails
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentVisitorsPageBinding
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
class VisitorsPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentVisitorsPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server

    private var allEmployees = ArrayList<EmployeeDetails>()
    private var allEmployeesName = ArrayList<String>()
    private val genders = arrayListOf("Male", "Female")
    private var allVisits = ArrayList<VisitorVisitDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentVisitorsPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        bind.allVisitsRcv.layoutManager = LinearLayoutManager(requireContext())

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        setActionListeners()
        getAllEmployees()
        getAllVisits(false)

        return bind.root
    }

    private fun getAllEmployees(){
        loadingDialog.startLoading()
        val response = server.getAllEmployees()
        response.enqueue(object: Callback<GetEmployeesResponse> {
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
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_text_list_item, genders.toTypedArray())
        bind.visitorGender.setAdapter(adapter)

        bind.showNewVisitorForm.setOnClickListener {
            bind.showNewVisitorForm.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showNewVisitorForm.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showAllVisitorsList.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showAllVisitorsList.setTextColor(Color.parseColor("#616161"))

            bind.allVisitsRcv.visibility = View.GONE
            bind.visitFormLayout.visibility = View.VISIBLE
        }

        bind.showAllVisitorsList.setOnClickListener {
            bind.showAllVisitorsList.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.showAllVisitorsList.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.showNewVisitorForm.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.showNewVisitorForm.setTextColor(Color.parseColor("#616161"))

            bind.allVisitsRcv.visibility = View.VISIBLE
            bind.visitFormLayout.visibility = View.GONE
        }

        bind.createVisitorButton.setOnClickListener {
            val employeeName = bind.employeeName.text.toString()
            val visitorName = bind.visitorName.text.toString()
            val visitorMobile = bind.visitorMobile.text.toString()
            val visitorAddress = bind.visitorAddress.text.toString()
            val visitorGender = bind.visitorGender.text.toString()
            val reason = bind.visitReason.text.toString()

            if(!allEmployeesName.contains(employeeName))
                Toast.makeText(requireContext(), "Select valid Employee", Toast.LENGTH_SHORT).show()
            else if(visitorName.isBlank())
                Toast.makeText(requireContext(), "Enter visitor name", Toast.LENGTH_SHORT).show()
            else if(visitorMobile.isBlank() || visitorMobile.length < 10)
                Toast.makeText(requireContext(), "Enter valid mobile number", Toast.LENGTH_SHORT).show()
            else if(visitorAddress.isBlank())
                Toast.makeText(requireContext(), "Enter visitor address", Toast.LENGTH_SHORT).show()
            else if(!genders.contains(visitorGender))
                Toast.makeText(requireContext(), "Select visitor's gender", Toast.LENGTH_SHORT).show()
            else if(reason.isBlank())
                Toast.makeText(requireContext(), "Enter visit reason", Toast.LENGTH_SHORT).show()
            else
                checkInVisit(employeeName, visitorName, visitorMobile, visitorAddress, visitorGender, reason)
        }
    }

    private fun checkInVisit(
        employeeName: String,
        visitorName: String,
        visitorMobile: String,
        visitorAddress: String,
        visitorGender: String,
        reason: String
    ) {
        loadingDialog.startLoading()

        var employeeId = 0
        try{
            employeeId = allEmployees.filter { it.employee_name?.contains(employeeName, true) == true || it.employee_number?.toString()?.contains(employeeName, true) == true }[0].employee_id ?: 0
        }catch (e: Exception){
            Log.d("XYZXYZ", e.localizedMessage?.toString() ?: "")
        }

        val response = server.visitorCheckIn(VisitorVisitData(employeeId, visitorAddress, reason, visitorGender.lowercase(), visitorMobile, visitorName))
        response.enqueue(object: Callback<VisitorCheckInResponse>{
            override fun onResponse(
                call: Call<VisitorCheckInResponse>,
                response: Response<VisitorCheckInResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    Toast.makeText(requireContext(), "Visitor added successfully", Toast.LENGTH_SHORT).show()
                    getAllVisits(false)
                    bind.employeeName.setText("")
                    bind.visitorName.setText("")
                    bind.visitorMobile.setText("")
                    bind.visitorAddress.setText("")
                    bind.visitorGender.setText("")
                    bind.visitReason.setText("")
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), VisitorCheckInResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VisitorCheckInResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkInVisit(employeeName, visitorName, visitorMobile, visitorAddress, visitorGender, reason)
                } else if (call.isCanceled) {
                    checkInVisit(employeeName, visitorName, visitorMobile, visitorAddress, visitorGender, reason)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getAllVisits(viewing: Boolean){
        if(viewing)
            loadingDialog.startLoading()

        allVisits.clear()
        val response = server.getAllVisitors()
        response.enqueue(object: Callback<GetAllVisitorVisitsResponse>{
            override fun onResponse(
                call: Call<GetAllVisitorVisitsResponse>,
                response: Response<GetAllVisitorVisitsResponse>
            ) {
                if(viewing)
                    loadingDialog.stopLoading()

                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty())
                        allVisits = result as ArrayList<VisitorVisitDetails>
                }else if(response.code()==404){
                    //Nothing
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GetAllVisitorVisitsResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }

                bind.allVisitsRcv.adapter = VisitorVisitsAdapter(allVisits, checkOut = {
                    checkOutVisitor(it.visitor_id ?: 0)
                }, cancel = {
                    cancelVisit(it.visitor_id ?: 0)
                })
            }

            override fun onFailure(call: Call<GetAllVisitorVisitsResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllVisits(viewing)
                } else if (call.isCanceled) {
                    getAllVisits(viewing)
                } else {
                    if (viewing)
                        loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun checkOutVisitor(visitorId: Int){
        loadingDialog.startLoading()

        val response = server.visitorCheckOut(VisitorId(visitorId))
        response.enqueue(object : Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    Toast.makeText(requireContext(), "Visitor checked-Out successfully", Toast.LENGTH_SHORT).show()
                    getAllVisits(true)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    checkOutVisitor(visitorId)
                } else if (call.isCanceled) {
                    checkOutVisitor(visitorId)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun cancelVisit(visitorId: Int){
        loadingDialog.startLoading()

        val response = server.cancelVisitorVisit(VisitorId(visitorId))
        response.enqueue(object : Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    Toast.makeText(requireContext(), "Visit canceled successfully", Toast.LENGTH_SHORT).show()
                    getAllVisits(true)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    cancelVisit(visitorId)
                } else if (call.isCanceled) {
                    cancelVisit(visitorId)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

}