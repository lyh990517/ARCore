package com.example.arcorestudy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.arcorestudy.databinding.ItemFaceBinding
import com.example.arcorestudy.rendering.FaceItem

class FaceItemAdapter : RecyclerView.Adapter<FaceItemAdapter.ViewHolder>() {
    val list = mutableListOf<FaceItem>()
    lateinit var listener: (FaceItem) -> Unit

    inner class ViewHolder(private val binding: ItemFaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(faceItem: FaceItem) {

            binding.root.setOnClickListener {
                listener(faceItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemFaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setItem(faceItem: FaceItem, listener: (FaceItem) -> Unit) {
        list.add(faceItem)
        this.listener = listener
        notifyDataSetChanged()
    }
}