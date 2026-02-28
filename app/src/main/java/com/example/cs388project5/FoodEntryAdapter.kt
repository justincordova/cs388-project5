package com.example.cs388project5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodEntryAdapter(
    private val foodEntries: MutableList<FoodEntryEntity>
) : RecyclerView.Adapter<FoodEntryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = foodEntries[position]
        holder.bind(entry)
    }

    override fun getItemCount() = foodEntries.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView = itemView.findViewById<TextView>(R.id.foodName)
        private val caloriesTextView = itemView.findViewById<TextView>(R.id.calories)

        fun bind(entry: FoodEntryEntity) {
            foodNameTextView.text = entry.name
            caloriesTextView.text = "${entry.calories} CALORIES"
        }
    }
}
