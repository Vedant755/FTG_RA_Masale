package com.ftg.famasale.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.ftg.famasale.Models.TruckDetails
import com.ftg.famasale.Models.VisitorVisitDetails
import com.ftg.famasale.R

class TruckListAdapter(
    private val trucks: List<TruckDetails>
) : RecyclerView.Adapter<TruckListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val truckNumber: TextView = itemView.findViewById(R.id.truck_number)
        val status: TextView = itemView.findViewById(R.id.truck_status)
        val driverName: TextView = itemView.findViewById(R.id.driver_name)
        val driverMobile: TextView = itemView.findViewById(R.id.driver_mobile)
        val description: TextView = itemView.findViewById(R.id.truck_description)
        val truckQty: TextView = itemView.findViewById(R.id.truck_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.truck_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trucks.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val truck = trucks[position]

        holder.truckNumber.text = truck.truck_number
        holder.status.text = truck.truck_status
        holder.driverName.text = truck.driver_name
        holder.driverMobile.text = truck.driver_mobile
        holder.description.text = truck.truck_description
        holder.truckQty.text = truck.quantity


        when (truck.truck_status){
            "null" -> {}
            "pending" -> { holder.status.setTextColor(Color.parseColor("#F9A825")) }
            "accept" -> {
                holder.status.setTextColor(Color.parseColor("#2E7D32"))
            }
            "reject" -> {
                holder.status.setTextColor(Color.parseColor("#D84315"))
            }
            else -> {}
        }
    }
}