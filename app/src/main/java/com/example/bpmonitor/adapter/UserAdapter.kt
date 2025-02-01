package com.example.bpmonitor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bpmonitor.R
import com.example.bpmonitor.model.User
import java.text.SimpleDateFormat
import java.util.*

private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

class UserAdapter(
    private val onItemClick: (User) -> Unit,
    private val onItemLongClick: (User) -> Boolean
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        itemView: View,
        private val onItemClick: (User) -> Unit,
        private val onItemLongClick: (User) -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.userName)
        private val dateText: TextView = itemView.findViewById(R.id.userCreatedDate)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        fun bind(user: User) {
            nameText.text = user.name
            dateText.text = dateFormat.format(Date(user.createdAt))
            itemView.setOnClickListener { onItemClick(user) }
            itemView.setOnLongClickListener { onItemLongClick(user) }
        }
    }
} 