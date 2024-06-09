package com.ftg.famasale.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ftg.famasale.Models.TruckDetails
import com.ftg.famasale.Models.VisitorVisitDetails
import com.ftg.famasale.R

class VisitorByEmployeeAdapter(
    private var visits: List<VisitorVisitDetails>,
    private val reject: (VisitorVisitDetails) -> Unit,
    private val accept: (VisitorVisitDetails) -> Unit
) : RecyclerView.Adapter<VisitorByEmployeeAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val visitorName: TextView = itemView.findViewById(R.id.visitor_name)
        val status: TextView = itemView.findViewById(R.id.status)
        val employeeName: TextView = itemView.findViewById(R.id.employee_name)
        val reject: AppCompatButton = itemView.findViewById(R.id.reject_button)
        val accept: AppCompatButton = itemView.findViewById(R.id.accept_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.visitor_view_employee, parent, false)
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
                holder.reject.visibility = View.GONE
            }
            "reject" -> {
                holder.status.setTextColor(Color.parseColor("#D84315"))
                holder.reject.visibility = View.GONE
            }
            else -> {}
        }

        holder.reject.setOnClickListener {
            reject(visit)
        }

        holder.accept.setOnClickListener {
            accept(visit)
        }
    }
    fun updateVisits(newVisits: List<VisitorVisitDetails>) {
        val diffResult = DiffUtil.calculateDiff(
            VisitorByEmployeeDiffCallback(
                visits,
                newVisits
            )
        )
        visits = newVisits
        diffResult.dispatchUpdatesTo(this)
    }
    class VisitorByEmployeeDiffCallback(private val oldList: List<VisitorVisitDetails>, private val newList: List<VisitorVisitDetails>) :
        DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].visitor_id == newList[newItemPosition].visitor_id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}