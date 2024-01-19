package com.ftg.famasale.Screens

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.famasale.Models.AllTruckListResponse
import com.ftg.famasale.Models.EmployeeVisitDetails
import com.ftg.famasale.Models.GetAllVisitorVisitsResponse
import com.ftg.famasale.Models.RequestedTruckData
import com.ftg.famasale.Models.TruckDetails
import com.ftg.famasale.Models.TruckRegisterResponse
import com.ftg.famasale.Models.VisitorCheckInResponse
import com.ftg.famasale.Models.VisitorVisitDetails
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentDispatchPageBinding
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
class DispatchPage : Fragment() {
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentDispatchPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server
    private var allTrucks = ArrayList<TruckDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentDispatchPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        getAllTrucks()
        setActionListeners()

        return bind.root
    }

    private fun setActionListeners(){
        bind.newVehicle.setOnClickListener {
            bind.newVehicle.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.newVehicle.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.allVehicles.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.allVehicles.setTextColor(Color.parseColor("#616161"))

            bind.rcv.visibility = View.GONE
            bind.newVehicleLayout.visibility = View.VISIBLE
        }

        bind.allVehicles.setOnClickListener {
            bind.allVehicles.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.allVehicles.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.newVehicle.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.newVehicle.setTextColor(Color.parseColor("#616161"))

            bind.rcv.visibility = View.VISIBLE
            bind.newVehicleLayout.visibility = View.GONE
        }

        bind.checkOutButton.setOnClickListener {
            val driverName = bind.driverName.text.toString()
            val driverMobile = bind.driverMobile.text.toString()
            val vehicleNumber = bind.vehicleNumber.text.toString()
            val gender = "Male"
            val vehicleDesc = bind.description.text.toString()

            if(driverName.isBlank())
                Toast.makeText(requireContext(), "Enter driver name!", Toast.LENGTH_SHORT).show()
            else if(vehicleNumber.isBlank())
                Toast.makeText(requireContext(), "Enter vehicle number!", Toast.LENGTH_SHORT).show()
            else if(driverMobile.isBlank() || driverMobile.length < 10)
                Toast.makeText(requireContext(), "Enter valid mobile number", Toast.LENGTH_SHORT).show()
            else{
                addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc)
            }
        }
    }

    private fun getAllTrucks(){
        allTrucks.clear()
        val response = server.getAllDispatchTrucks()
        response.enqueue(object: Callback<AllTruckListResponse>{
            override fun onResponse(
                call: Call<AllTruckListResponse>,
                response: Response<AllTruckListResponse>
            ) {
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty())
                        allTrucks = result as ArrayList<TruckDetails>
                }else if(response.code()==404){
                    //Nothing
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), AllTruckListResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AllTruckListResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllTrucks()
                } else if (call.isCanceled) {
                    getAllTrucks()
                } else {
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addVehicle(
        driverName: String,
        driverMobile: String,
        vehicleNumber: String,
        gender: String,
        vehicleDesc: String
    ) {
        loadingDialog.startLoading()

        val response = server.registerDispatchTruck(RequestedTruckData(gender, driverMobile, driverName, vehicleDesc, vehicleNumber, null))
        response.enqueue(object: Callback<TruckRegisterResponse>{
            override fun onResponse(
                call: Call<TruckRegisterResponse>,
                response: Response<TruckRegisterResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    Toast.makeText(requireContext(), "Vehicle added successfully", Toast.LENGTH_SHORT).show()
                    bind.driverName.setText("")
                    bind.driverMobile.setText("")
                    bind.vehicleNumber.setText("")
                    bind.description.setText("")
                    getAllTrucks()
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), TruckRegisterResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TruckRegisterResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc)
                } else if (call.isCanceled) {
                    addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


}