package com.ftg.famasale.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.ftg.famasale.Models.CheckedOutVehicleDetails
import com.ftg.famasale.Models.EmployeeDetails
import com.ftg.famasale.R

class VehicleCheckInAdapter(
    private val vehicles: List<CheckedOutVehicleDetails>,
    private val checkIn: (CheckedOutVehicleDetails) -> Unit
) : RecyclerView.Adapter<VehicleCheckInAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vehicleNumber: TextView = itemView.findViewById(R.id.vehicle_number)
        val employeeName: TextView = itemView.findViewById(R.id.employee_name)
        val action: AppCompatButton = itemView.findViewById(R.id.check_in)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vehicle_check_in_tem, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicle = vehicles[position]

        holder.vehicleNumber.text = vehicle.Vehicle?.vehicle_rc ?: vehicle.Vehicle?.vehicle_id.toString()
        holder.employeeName.text = vehicle.Employee?.employee_name ?: vehicle.Employee?.employee_id.toString()

        holder.action.setOnClickListener { checkIn(vehicle) }
    }
}