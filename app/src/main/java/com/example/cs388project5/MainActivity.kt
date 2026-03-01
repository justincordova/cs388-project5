package com.example.cs388project5

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
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
    private lateinit var averageText: TextView
    private val foodEntries = mutableListOf<FoodEntryEntity>()
    private lateinit var foodEntryAdapter: FoodEntryAdapter
    private var photoUri: Uri? = null
    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        photoUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        foodRecyclerView = binding.foodRecyclerView
        addButton = binding.addButton
        averageText = binding.averageText

        foodEntryAdapter = FoodEntryAdapter(foodEntries)
        foodRecyclerView.adapter = foodEntryAdapter
        foodRecyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val entry = foodEntries[position]
                deleteEntry(entry.id)
            }
        })

        itemTouchHelper.attachToRecyclerView(foodRecyclerView)

        observeFoodEntries()

        addButton.setOnClickListener {
            showAddEntryDialog()
        }
    }

    private fun observeFoodEntries() {
        lifecycleScope.launch {
            (application as BitFitApplication).db.foodEntryDao().getAll().collect { databaseList ->
                android.util.Log.d("DEBUG", "Database list size: ${databaseList.size}")
                databaseList.forEachIndexed { index, entry ->
                    android.util.Log.d("DEBUG", "Entry $index: id=${entry.id}, name=${entry.name}, calories=${entry.calories}")
                }

                val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(
                    FoodEntryDiffCallback(foodEntries, databaseList)
                )

                foodEntries.clear()
                foodEntries.addAll(databaseList)

                android.util.Log.d("DEBUG", "After clear/add: foodEntries size=${foodEntries.size}")
                foodEntries.forEachIndexed { index, entry ->
                    android.util.Log.d("DEBUG", "foodEntry $index: id=${entry.id}, name=${entry.name}, calories=${entry.calories}")
                }

                val average = if (databaseList.isNotEmpty()) {
                    databaseList.sumOf { it.calories } / databaseList.size
                } else {
                    0
                }
                averageText.text = getString(R.string.average_calories, average)

                diffResult.dispatchUpdatesTo(foodEntryAdapter)
            }
        }
    }

    private class FoodEntryDiffCallback(
        private val oldList: List<FoodEntryEntity>,
        private val newList: List<FoodEntryEntity>
    ) : androidx.recyclerview.widget.DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }

    private fun showAddEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_entry, null)
        val foodNameInput = dialogView.findViewById<TextInputEditText>(R.id.foodNameInput)
        val caloriesInput = dialogView.findViewById<TextInputEditText>(R.id.caloriesInput)
        val photoButton = dialogView.findViewById<Button>(R.id.photoButton)

        photoButton.setOnClickListener {
            pickPhoto.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.add_food_entry)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val foodName = foodNameInput.text?.toString()?.trim()
                val calories = caloriesInput.text?.toString()?.toIntOrNull()

                if (foodName.isNullOrEmpty() || calories == null) {
                    Toast.makeText(this, R.string.please_enter_valid_values, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addEntry(foodName, calories, photoUri?.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addEntry(name: String, calories: Int, photoUri: String? = null) {
        val entry = FoodEntryEntity.create(name, calories, photoUri)
        android.util.Log.d("DEBUG", "Adding entry: name=$name, calories=$calories, photoUri=$photoUri")
        lifecycleScope.launch(Dispatchers.IO) {
            val id = (application as BitFitApplication).db.foodEntryDao().insert(entry)
            android.util.Log.d("DEBUG", "Inserted entry with id=$id")
        }
    }

    private fun deleteEntry(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            (application as BitFitApplication).db.foodEntryDao().delete(id)
        }
    }
}