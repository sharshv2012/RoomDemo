package com.example.roomdemo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.R
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener{
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAllEmployee().collect(){
                val list = ArrayList(it)
                setUpListOfDataInRecyclerView(list , employeeDao)
            }
        }
    }
    fun addRecord(employeeDao: EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()
        if (name.isNotEmpty() && email.isNotEmpty()){
            lifecycleScope.launch{
                employeeDao.insert(EmployeeEntity(name = name , email = email))
                Toast.makeText(applicationContext , "Record Saved" , Toast.LENGTH_LONG ).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        }else{
            Toast.makeText(applicationContext , "Name or Email cannot be Blank" , Toast.LENGTH_LONG ).show()

        }


    }

    private fun setUpListOfDataInRecyclerView(employeesList:ArrayList<EmployeeEntity> ,
    employeeDao: EmployeeDao){
        if (employeesList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeesList, {
                updateId ->
                updateRecordDialog(updateId , employeeDao)
                } ,
                {
                    deleteId ->
                    deleteRecordAlertDialog(deleteId , employeeDao)
                }
            )
            binding?.rvRcrdsList?.layoutManager =LinearLayoutManager(this)
            binding?.rvRcrdsList?.adapter = itemAdapter
            binding?.rvRcrdsList?.visibility = View.VISIBLE
            binding?.tvNoRcrdsAvailble?.visibility = View.GONE
        }else{
            binding?.rvRcrdsList?.visibility = View.GONE
            binding?.tvNoRcrdsAvailble?.visibility = View.VISIBLE
        }
    }


    private fun updateRecordDialog(id : Int , employeeDao: EmployeeDao){
        val updateDialog = Dialog(this ,com.example.roomdemo.R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect(){
                if(it != null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }
        binding.tvUpdate.setOnClickListener{
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id , name ,email))
                    Toast.makeText(applicationContext , "record Updated",
                    Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(applicationContext , "Fields can't be empty.",
                    Toast.LENGTH_LONG).show()
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    private  fun deleteRecordAlertDialog(id : Int , employeeDao: EmployeeDao){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface , _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext , "Record Deleted Successfully"
                , Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()

        }
        builder.setNegativeButton("No"){dialogInterface , _ ->
            dialogInterface.dismiss()

        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}