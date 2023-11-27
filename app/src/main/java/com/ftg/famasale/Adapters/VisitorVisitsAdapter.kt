package com.ftg.famasale.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.ftg.famasale.Models.CheckedOutVehicleDetails
import com.ftg.famasale.Models.EmployeeDetails
import com.ftg.famasale.Models.VisitorVisitDetails
import com.ftg.famasale.R

class VisitorVisitsAdapter(
    private val visits: List<VisitorVisitDetails>,
    private val checkOut: (VisitorVisitDetails) -> Unit,
    private val cancel: (VisitorVisitDetails) -> Unit
) : RecyclerView.Adapter<VisitorVisitsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val visitorName: TextView = itemView.findViewById(R.id.visitor_name)
        val status: TextView = itemView.findViewById(R.id.status)
        val employeeName: TextView = itemView.findViewById(R.id.employee_name)
        val checkOut: AppCompatButton = itemView.findViewById(R.id.check_out_button)
        val cancel: AppCompatButton = itemView.findViewById(R.id.cancel_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.visitor_visit_tem, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return visits.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visit = visits[position]

        holder.visitorName.text = "${visit.visitor_name} / ${visit.visitor_mobile}"
        holder.employeeName.text = "Employee: ${visit.Employee?.employee_name ?: visit.Employee?.employee_number.toString()}"
        holder.status.text = visit.visitor_status

        when (visit.visitor_status.toString()){
            "null" -> {}
            "pending" -> { holder.status.setTextColor(Color.parseColor("#F9A825")) }
            "accept" -> {
                holder.status.setTextColor(Color.parseColor("#2E7D32"))
                holder.cancel.visibility = View.GONE
            }
            "rejected" -> {
                holder.status.setTextColor(Color.parseColor("#D84315"))
                holder.cancel.visibility = View.GONE
            }
            else -> {}
        }

        holder.checkOut.setOnClickListener {
            checkOut(visit)
        }

        holder.cancel.setOnClickListener {
            cancel(visit)
        }
    }
}