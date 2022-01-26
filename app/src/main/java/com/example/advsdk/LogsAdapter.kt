package com.example.advsdk

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView


@SuppressLint("NotifyDataSetChanged")
class LogsAdapter :
    RecyclerView.Adapter<LogsAdapter.LogsHolder>() {

    val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsHolder {
        return LogsHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_logs, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LogsHolder, position: Int) {
        holder.bind(items[position])
    }

    fun addLog(log: String) {
        items.add(log)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    inner class LogsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) {
            itemView.findViewById<AppCompatTextView>(R.id.tvLog).text = item
        }
    }
}