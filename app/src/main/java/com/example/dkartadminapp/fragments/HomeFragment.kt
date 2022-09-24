package com.example.dkartadminapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dkartadminapp.R
import com.example.dkartadminapp.allActivityes.AllOrderActivity
import com.example.dkartadminapp.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)



        binding.addCategoryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment)
        }

        binding.addOrderDetailBtn.setOnClickListener {
        startActivity(Intent(context,AllOrderActivity::class.java))
        }

        binding.addProductBtn.setOnClickListener {
              findNavController().navigate(R.id.action_homeFragment_to_productFragment)
        }

        binding.addSliderBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sliderFragment)
        }
        return  binding.root
    }



}