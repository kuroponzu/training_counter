package com.example.training_counter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(private val records: List<PeriodRecord>) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val periodNumberTextView: TextView = itemView.findViewById(R.id.periodNumberTextView)
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        val timeInfoTextView: TextView = itemView.findViewById(R.id.timeInfoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        
        holder.periodNumberTextView.text = "ピリオド ${record.periodNumber}"
        holder.countTextView.text = "${record.count}回"
        holder.timeInfoTextView.text = "${record.getFormattedStartTime()} - ${record.getFormattedEndTime()} (${record.getFormattedDuration()})"
    }

    override fun getItemCount(): Int = records.size
}