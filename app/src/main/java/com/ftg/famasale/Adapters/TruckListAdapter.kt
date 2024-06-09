package com.ftg.famasale.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ftg.famasale.Models.TruckDetails
import com.ftg.famasale.R

class TruckListAdapter(
    private var trucks: List<TruckDetails>,
    private var departmentNameIsDispatch: Boolean,
    private val out: (TruckDetails) -> Unit,
    private val completed: (TruckDetails) -> Unit,
    private val authorized:Boolean
) : RecyclerView.Adapter<TruckListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val truckNumber: TextView = itemView.findViewById(R.id.truck_number)
        val status: TextView = itemView.findViewById(R.id.truck_status)
        val driverName: TextView = itemView.findViewById(R.id.driver_name)
        val driverMobile: TextView = itemView.findViewById(R.id.driver_mobile)
        val description: TextView = itemView.findViewById(R.id.truck_description)
        val truckQty: TextView = itemView.findViewById(R.id.truck_quantity)
        val out: AppCompatButton = itemView.findViewById(R.id.out)
        val completed: AppCompatButton = itemView.findViewById(R.id.completed)
        val authorized: View = itemView.findViewById(R.id.onlyauthorized)
        val remark: EditText = itemView.findViewById(R.id.remark)
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
        holder.status.text = "Status: ${truck.truck_status}"
        holder.driverName.text = "Name: ${truck.driver_name}"
        holder.driverMobile.text = "Phone: ${truck.driver_mobile}"
        holder.description.text = "Desc: ${truck.truck_description}"
        holder.truckQty.text = "Quantity: ${truck.quantity}"

        Log.d("onBindViewHolder", "Department Name Is Dispatch: $departmentNameIsDispatch")
        Log.d("onBindViewHolder", "Authorized: $authorized")

        if (departmentNameIsDispatch) {
            holder.remark.visibility = View.VISIBLE
        }else{
            holder.remark.visibility=View.GONE
            holder.out.text ="Accept"
            holder.completed.text ="Cancel"
        }

        if (authorized) {
            holder.authorized.visibility = View.VISIBLE
        } else {
            holder.authorized.visibility = View.GONE
        }

        when (truck.truck_status) {
            "null" -> { /* no color change */ }
            "pending" -> holder.status.setTextColor(Color.parseColor("#F9A825"))
            "accept" -> holder.status.setTextColor(Color.parseColor("#2E7D32"))
            "cancel" -> holder.status.setTextColor(Color.parseColor("#D84315"))
            else -> { /* no color change */ }
        }

        holder.out.setOnClickListener { out(truck) }
        holder.completed.setOnClickListener { completed(truck) }
    }

    fun updateTrucks(newTrucks: List<TruckDetails>) {
        val diffResult = DiffUtil.calculateDiff(TruckDiffCallback(trucks, newTrucks))
        trucks = newTrucks
        diffResult.dispatchUpdatesTo(this)
    }
    class TruckDiffCallback(private val oldList: List<TruckDetails>, private val newList: List<TruckDetails>) :
        DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].truck_id == newList[newItemPosition].truck_id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}