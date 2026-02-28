package com.example.cs388project5

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs388project5.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private lateinit var addButton: Button
    private val foodEntries = mutableListOf<FoodEntryEntity>()
    private lateinit var foodEntryAdapter: FoodEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        foodRecyclerView = binding.foodRecyclerView
        addButton = binding.addButton

        foodEntryAdapter = FoodEntryAdapter(foodEntries)
        foodRecyclerView.adapter = foodEntryAdapter
        foodRecyclerView.layoutManager = LinearLayoutManager(this)

        observeFoodEntries()

        addButton.setOnClickListener {
            showAddEntryDialog()
        }
    }

    private fun observeFoodEntries() {
        lifecycleScope.launch {
            (application as BitFitApplication).db.foodEntryDao().getAll().collect { databaseList ->
                val oldSize = foodEntries.size
                foodEntries.clear()
                foodEntries.addAll(databaseList)
                if (foodEntries.size > oldSize) {
                    foodEntryAdapter.notifyItemRangeInserted(oldSize, foodEntries.size - oldSize)
                } else {
                    foodEntryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun showAddEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_entry, null)
        val foodNameInput = dialogView.findViewById<TextInputEditText>(R.id.foodNameInput)
        val caloriesInput = dialogView.findViewById<TextInputEditText>(R.id.caloriesInput)

        AlertDialog.Builder(this)
            .setTitle("Add Food Entry")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val foodName = foodNameInput.text?.toString()?.trim()
                val calories = caloriesInput.text?.toString()?.toIntOrNull()

                if (foodName.isNullOrEmpty() || calories == null) {
                    Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addEntry(foodName, calories)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addEntry(name: String, calories: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            (application as BitFitApplication).db.foodEntryDao().insert(
                FoodEntryEntity.create(name, calories)
            )
        }
    }
}