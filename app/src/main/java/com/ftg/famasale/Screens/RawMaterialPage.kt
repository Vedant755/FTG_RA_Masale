package com.ftg.famasale.Screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.famasale.Models.RequestedTruckData
import com.ftg.famasale.Models.TruckRegisterResponse
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.databinding.FragmentDispatchPageBinding
import com.ftg.famasale.databinding.FragmentRawMaterialPageBinding
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
class RawMaterialPage : Fragment() {
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentRawMaterialPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentRawMaterialPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        setActionListeners()

        return bind.root
    }

    private fun setActionListeners(){
        bind.allVehicles.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
        }

        bind.checkOutButton.setOnClickListener {
            val driverName = bind.driverName.text.toString()
            val driverMobile = bind.driverMobile.text.toString()
            val vehicleNumber = bind.vehicleNumber.text.toString()
            val quantity = bind.quantity.text.toString()
            val gender = "Male"
            val vehicleDesc = bind.description.text.toString()

            if(driverName.isBlank())
                Toast.makeText(requireContext(), "Enter driver name!", Toast.LENGTH_SHORT).show()
            else if(vehicleNumber.isBlank())
                Toast.makeText(requireContext(), "Enter vehicle number!", Toast.LENGTH_SHORT).show()
            else if(driverMobile.isBlank() || driverMobile.length < 10)
                Toast.makeText(requireContext(), "Enter valid mobile number!", Toast.LENGTH_SHORT).show()
            else if(quantity.isBlank())
                Toast.makeText(requireContext(), "Enter quantity!", Toast.LENGTH_SHORT).show()
            else{
                addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc, quantity)
            }
        }
    }

    private fun addVehicle(
        driverName: String,
        driverMobile: String,
        vehicleNumber: String,
        gender: String,
        vehicleDesc: String,
        quantity: String
    ) {
        loadingDialog.startLoading()

        val response = server.registerRawMaterialTruck(RequestedTruckData(gender, driverMobile, driverName, vehicleDesc, vehicleNumber, quantity))
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
                    bind.quantity.setText("")
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), TruckRegisterResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TruckRegisterResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc, quantity)
                } else if (call.isCanceled) {
                    addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc, quantity)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}