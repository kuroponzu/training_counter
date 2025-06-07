package com.example.training_counter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(private val records: List<Int>) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val periodNumberTextView: TextView = itemView.findViewById(R.id.periodNumberTextView)
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val periodNumber = position + 1
        val count = records[position]
        
        holder.periodNumberTextView.text = "ピリオド $periodNumber"
        holder.countTextView.text = "${count}回"
    }

    override fun getItemCount(): Int = records.size
}