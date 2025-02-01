package com.example.bpmonitor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.bpmonitor.data.BPDatabase
import com.example.bpmonitor.model.User
import com.example.bpmonitor.adapter.UserAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var db: BPDatabase
    private lateinit var adapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        db = Room.databaseBuilder(
            applicationContext,
            BPDatabase::class.java,
            "bp_database"
        ).build()
        
        setupRecyclerView()
        setupAddButton()
        loadUsers()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.userList)
        adapter = UserAdapter(
            onItemClick = { user ->
                // 点击进入血压记录界面
                val intent = Intent(this, BPRecordActivity::class.java).apply {
                    putExtra("userId", user.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { user ->
                // 长按删除用户
                showDeleteConfirmDialog(user)
                true
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupAddButton() {
        findViewById<Button>(R.id.addUserButton).setOnClickListener {
            showAddUserDialog()
        }
    }
    
    private fun showAddUserDialog() {
        val editText = EditText(this).apply {
            hint = "请输入用户名"
        }
        
        AlertDialog.Builder(this)
            .setTitle("添加新用户")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val userName = editText.text.toString()
                if (userName.isNotBlank()) {
                    lifecycleScope.launch {
                        db.userDao().insertUser(User(name = userName))
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun loadUsers() {
        lifecycleScope.launch {
            db.userDao().getAllUsers().collectLatest { users ->
                adapter.submitList(users)
            }
        }
    }
    
    private fun showDeleteConfirmDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("删除用户")
            .setMessage("确定要删除用户\"${user.name}\"吗？\n删除后，该用户的所有血压记录也将被删除。")
            .setPositiveButton("删除") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            try {
                // 先删除用户的血压记录
                db.bloodPressureDao().deleteUserBPRecords(user.id)
                // 再删除用户
                db.userDao().deleteUser(user)
                Toast.makeText(
                    this@MainActivity,
                    "用户\"${user.name}\"已删除",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "删除失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
} 