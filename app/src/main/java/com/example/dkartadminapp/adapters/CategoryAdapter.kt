package com.example.dkartadminapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dkartadminapp.R
import com.example.dkartadminapp.databinding.CategoryItemFormateBinding
import com.example.dkartadminapp.models.CategoryModel

class CategoryAdapter(var contextt: Context,var categorylist:ArrayList<CategoryModel>) :RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){

    inner class CategoryViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding = CategoryItemFormateBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
       return CategoryViewHolder(LayoutInflater.from(contextt).inflate(R.layout.category_item_formate,parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
       var categoryobj = categorylist[position]

        Glide.with(contextt).load(categoryobj.img).into(holder.binding.categoryImageRecyclerview)
          holder.binding.categoryNameView.text = categoryobj.cateGory


    }

    override fun getItemCount(): Int {
        return categorylist.size
    }
}