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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ftg.famasale.Adapters.TruckListAdapter
import com.ftg.famasale.Models.AllTruckListResponse
import com.ftg.famasale.Models.AllVehiclesResponse
import com.ftg.famasale.Models.GeneralResponse
import com.ftg.famasale.Models.RequestedTruckData
import com.ftg.famasale.Models.TruckDetails
import com.ftg.famasale.Models.TruckRegisterResponse
import com.ftg.famasale.Models.TruckRequestData
import com.ftg.famasale.Models.VehicleDetails
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
    private var allVehicleNames = ArrayList<String>()
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private lateinit var filterRadioGroup: RadioGroup
    private lateinit var radioPending: RadioButton
    private lateinit var radioConfirmed: RadioButton
    private lateinit var radioCancelled: RadioButton
    private var allVehicles = ArrayList<VehicleDetails>()
    private lateinit var bind: FragmentDispatchPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server
    private var allTrucks = ArrayList<TruckDetails>()
    private lateinit var adapter : TruckListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentDispatchPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        val layoutManager = LinearLayoutManager(requireContext())
        bind.rcv.layoutManager = layoutManager
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)
        filterRadioGroup = bind.filterRadioGroup
        radioPending = bind.radioPending
        radioConfirmed = bind.radioConfirmed
        radioCancelled = bind.radioCancelled
        getAllTrucks(false)
        getAllVehicles()
        setActionListeners()

        return bind.root
    }

    private fun setActionListeners(){
        if(sharedPrefManager.getAuthority()?.contains("department") == true){
            bind.rcv.visibility = View.VISIBLE
            bind.newVehicleLayout.visibility = View.GONE
            bind.newVehicle.visibility = View.GONE
            bind.filterRadioGroup.visibility = View.VISIBLE
            bind.allVehicles.visibility= View.GONE
            bind.refreshButton.visibility = View.VISIBLE
        }
        bind.newVehicle.setOnClickListener {
            bind.newVehicle.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.newVehicle.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.allVehicles.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.allVehicles.setTextColor(Color.parseColor("#616161"))
            bind.rcv.visibility = View.GONE
            bind.filterRadioGroup.visibility = View.GONE
            bind.newVehicleLayout.visibility = View.VISIBLE
        }
        bind.allVehicles.setOnClickListener {
            bind.allVehicles.background.setColorFilter(Color.parseColor("#0277BD"), PorterDuff.Mode.SRC_ATOP)
            bind.allVehicles.setTextColor(Color.parseColor("#FFFFFFFF"))
            bind.newVehicle.background.setColorFilter(Color.parseColor("#FFE0F7FA"), PorterDuff.Mode.SRC_ATOP)
            bind.newVehicle.setTextColor(Color.parseColor("#616161"))
            bind.rcv.visibility = View.VISIBLE
            bind.filterRadioGroup.visibility = View.VISIBLE
            bind.newVehicleLayout.visibility = View.GONE
            bind.refreshButton.visibility = View.VISIBLE
        }
        bind.checkOutButton.setOnClickListener {
            val driverName = bind.driverName.text.toString()
            val driverMobile = bind.driverMobile.text.toString()
            val driverNumber_extra = bind.driverVehicleNumber.text.toString()
            val vehicleNumber = bind.vehicleNumber.text.toString()
            var realvehicleNumber = ""
            val gender = "male"
            val vehicleDesc = bind.description.text.toString()
            if (vehicleNumber.isBlank() && driverNumber_extra.isNotBlank()){
                realvehicleNumber=driverNumber_extra
            }else if(vehicleNumber.isNotBlank() && driverNumber_extra.isBlank()){
                realvehicleNumber=vehicleNumber
            }else if(vehicleNumber.isBlank() && driverNumber_extra.isBlank()){
                Toast.makeText(requireContext(), "Enter vehicle number!", Toast.LENGTH_SHORT).show()
            }
            if(driverName.isBlank())
                Toast.makeText(requireContext(), "Enter driver name!", Toast.LENGTH_SHORT).show()

            else if(driverMobile.isBlank() || driverMobile.length < 10)
                Toast.makeText(requireContext(), "Enter valid mobile number!", Toast.LENGTH_SHORT).show()
            else if(realvehicleNumber == ""){
                Toast.makeText(requireContext(), "Enter vehicle number!", Toast.LENGTH_SHORT).show()
            }
            else{
                addVehicle(driverName, driverMobile, realvehicleNumber, gender, vehicleDesc)
            }
        }
        bind.refreshButton.setOnClickListener{
            getAllTrucks(true)
        }
        filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioPending -> filterOrdersByStatus("Pending")
                R.id.radioConfirmed -> filterOrdersByStatus("accept")
                R.id.radioCancelled -> filterOrdersByStatus("cancel")
            }
        }
    }
    private fun filterOrdersByStatus(status: String) {
        val filteredTrucks = allTrucks.filter { it.truck_status.equals(status, ignoreCase = true) }
        adapter.updateTrucks(filteredTrucks)
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
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_text_list_item, allVehicleNames.toTypedArray())
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
    private fun getAllTrucks(viewing: Boolean){
        allTrucks.clear()
        if (viewing)
            loadingDialog.startLoading()
        var pendingCount = 0
        var acceptedCount = 0
        var cancelledCount = 0
        val authorized:Boolean = sharedPrefManager.getAuthority()?.contains("department") == true
        val response = server.getAllDispatchTrucks()
        response.enqueue(object: Callback<AllTruckListResponse>{
            override fun onResponse(
                call: Call<AllTruckListResponse>,
                response: Response<AllTruckListResponse>
            ) {
                if (viewing)
                    loadingDialog.stopLoading()
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty())
                        allTrucks = result as ArrayList<TruckDetails>
                    for (truck in allTrucks) {
                        when (truck.truck_status) {
                            "pending" -> pendingCount++
                            "accept" -> acceptedCount++
                            "cancel" -> cancelledCount++
                        }
                    }
                    filterRadioGroup.clearCheck()
                    radioPending.text = getString(R.string.radio_pending, pendingCount)

                    radioConfirmed.text = getString(R.string.radio_confirmed, acceptedCount)

                    radioCancelled.text = getString(R.string.radio_cancelled, cancelledCount)
                }else if(response.code()==404){
                    //Nothing
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), AllTruckListResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }

                adapter= TruckListAdapter(allTrucks,
                    sharedPrefManager.getUserDetails().department_name == "DISPATCH",
                    out = {
                    accept(it.truck_id?:0)
                },  completed = {
                    reject(it.truck_id?:0)
                },authorized)
                bind.rcv.adapter = adapter
            }

            override fun onFailure(call: Call<AllTruckListResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllTrucks(viewing)
                } else if (call.isCanceled) {
                    getAllTrucks(viewing)
                } else {
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun accept(truckId: Int){
        loadingDialog.startLoading()

        val response = server.updateDispatchTruckStatus(TruckRequestData("accept", truck_id = truckId))
        response.enqueue(object : Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                Log.d("Dispatch Response",response.body().toString())

                if(response.code()==200){
                    Log.d("Dispatch Response",response.body().toString())
                    Toast.makeText(requireContext(), "Truck Accepted", Toast.LENGTH_SHORT).show()
                    getAllTrucks(true)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    accept(truckId)
                } else if (call.isCanceled) {
                    accept(truckId)
                } else {
                    loadingDialog.stopLoading()
                    Log.e("mes",t.localizedMessage?:"Something")
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun reject(truckId: Int){
        loadingDialog.startLoading()

        val response = server.updateDispatchTruckStatus(TruckRequestData("cancel", truck_id = truckId))
        response.enqueue(object : Callback<GeneralResponse>{
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==200){
                    Toast.makeText(requireContext(), "Truck Completed successfully", Toast.LENGTH_SHORT).show()
                    getAllTrucks(true)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GeneralResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    reject(truckId)
                } else if (call.isCanceled) {
                    reject(truckId)
                } else {
                    loadingDialog.stopLoading()
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
                    getAllTrucks(false)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), TruckRegisterResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TruckRegisterResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    addVehicle(driverName, driverMobile, vehicleNumber, gender, vehicleDesc )
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