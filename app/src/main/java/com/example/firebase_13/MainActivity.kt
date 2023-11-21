package com.example.firebase_13

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import com.example.firebase_13.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    // inisiasi main binding
    lateinit var binding: ActivityMainBinding

    //firebase
    private val firebase = FirebaseFirestore.getInstance()
    private val budgetColllectionRef = firebase.collection("budgets")
    private var updateId = " "
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>> ()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            // menambah data di form
            btnAdd.setOnClickListener {
                println("this running on add")
                val nominal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()
                // budget atau data yang berisi nominal, desc, dan date yang diisikan sebelumnya di data class budget
                val newBudget = Budget (
                    nominal = nominal,
                    desc = desc,
                    date = date
                )
                // ketika data berhasil dimasukkan di form, klik addData untuk menambahkannya di database
                addData(newBudget)
            }

            // update data di form
            btnUpdate.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val desc = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val budgetUpdate = Budget (
                    nominal = nominal,
                    desc = desc,
                    date = date
                )
                // ketika data berhasil di update maka form akan ke-reset
                updateData(budgetUpdate)
                updateId = " "
                resetForm()
            }

            // klik item untuk update
            listView.setOnItemClickListener { viewAdapter, view, position, id ->
                val item = viewAdapter.adapter.getItem(position) as Budget
                updateId = item.id
                edtNominal.setText(item.nominal)
                edtDesc.setText(item.desc)
                edtDate.setText(item.date)
            }

            // klik lama untuk menghapus data
            listView.setOnItemLongClickListener { viewAdapter, view, position, id ->
                val item = viewAdapter.adapter.getItem(position) as Budget
                deleteData(item)
                true
            }

        }

        observeBudgets()
        getAllBudget()
    }

    // mengambil semua data
    private fun getAllBudget() {
        observeBudgetChanges()
    }

    // perubahan dalam suatu koleksi Firestore
    private fun observeBudgetChanges() {
        budgetColllectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.d("MainActivity", "Error Listening for budget")
                return@addSnapshotListener
            }
            val budgets = snapshot?.toObjects(Budget::class.java)
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    // mengamati perubahan dalam objek LiveData yang disebut budgetListLiveData.
    private fun observeBudgets() {
        budgetListLiveData.observe(this) {
            budgets ->
            val adapter = ArrayAdapter (
                this,
                android.R.layout.simple_list_item_1,
                budgets.toMutableList()
            )
            binding.listView.adapter = adapter
        }
    }

    // function menambah data
    private fun addData(budget: Budget) {
        println("running on add")
        budgetColllectionRef.add(budget)
            .addOnSuccessListener { docRef ->
                val createBudgetId = docRef.id
                // update id berdasarkan id yang di update tadi
                budget.id = createBudgetId
                docRef.set(budget)
                    .addOnFailureListener {
                        println("this on failed empty id")
                        Log.d("MainActivity", "Error update budget id", it)
                    }
                resetForm()
            }
            .addOnFailureListener {
                println("this on failed add data")
                Log.d("MainActivity", "Error add budget", it)
            }
    }

    // function mereset form
    private fun resetForm() {
        with(binding) {
            edtNominal.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }

    // function update data
    private fun updateData(budget: Budget) {
        budget.id = updateId
        budgetColllectionRef.document(updateId).set(budget)
            .addOnFailureListener {
                Log.d("MainActivity", "Error update data budget", it)
            }
    }

    // function delete data
    private fun deleteData(budget: Budget) {
        // cek id kosong apa egk
        if (budget.id.isEmpty()) {
            Log.d("MainActivity", "Error delete data empty IO", return)
        }
        budgetColllectionRef.document(budget.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error delete data budget", it)
            }
    }

}