package com.example.dkartadminapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dkartadminapp.R
import com.example.dkartadminapp.databinding.SelectImageFormateBinding

class SelelctedImageAdapter(var photolist:ArrayList<Uri>):RecyclerView.Adapter<SelelctedImageAdapter.SelectImageViewHolder>() {

    inner class SelectImageViewHolder(val binding:SelectImageFormateBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectImageViewHolder {
     return SelectImageViewHolder(SelectImageFormateBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: SelectImageViewHolder, position: Int) {
        holder.binding.selectedImageview.setImageURI(photolist[position])
    }

    override fun getItemCount(): Int {
        return photolist.size
    }
}