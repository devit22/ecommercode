package com.example.dkartadminapp.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dkartadminapp.R
import com.example.dkartadminapp.adapters.CategoryAdapter
import com.example.dkartadminapp.databinding.FragmentCategoryBinding
import com.example.dkartadminapp.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class CategoryFragment : Fragment() {

private lateinit var binding:FragmentCategoryBinding


    private  var imageUrl: Uri? = null

    private lateinit var diaglog: Dialog
    private lateinit var dbstorage: FirebaseStorage
    val permisions:Array<String> = arrayOf(
        Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    val permissioncode = 140201
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        dbstorage = FirebaseStorage.getInstance()
        diaglog = Dialog(requireContext())
        diaglog.setContentView(R.layout.progress_layout)
        diaglog.setCancelable(false)
      //  intailisecomponent()
       getdata()

        binding.apply {
            pickCategoryImage.setOnClickListener {

                if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permisions,permissioncode)
                }else{
                    val popupMenu = PopupMenu(context,pickCategoryImage)
                    popupMenu.inflate(R.menu.imagemenu)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.camerasourch ->{
                                val intent = Intent()

                                intent.action = MediaStore.ACTION_IMAGE_CAPTURE

                                CamereResultLauncher.launch(intent)
                            }
                            R.id.fromgallery ->{
                                val intent = Intent()
                                intent.action = Intent.ACTION_GET_CONTENT
                                intent.type = "image/*"

                                launchGallaryActivity.launch(intent)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
            }

            addUploadCategoryBtn.setOnClickListener {

                val categoryText = binding.categoryEdittext.text.toString()

                if(TextUtils.isEmpty(categoryText)){
                    Toast.makeText(requireContext(),"Please enter category name",Toast.LENGTH_SHORT).show()
                }else if(imageUrl == null){
                    Toast.makeText(requireContext(),"Please select image",Toast.LENGTH_SHORT).show()
                }else{
                    uploadimageurl(imageUrl!!,categoryText)
                }
            }
        }
        return  binding.root
    }

//    private fun intailisecomponent() {
//        binding.categoryRecyclerview.layoutManager = LinearLayoutManager(requireContext())
//    }

    private fun getdata() {
        var list = ArrayList<CategoryModel>()

        Firebase.firestore.collection("category")
            .get().addOnSuccessListener {
                if(it.isEmpty){
                    Toast.makeText(requireContext(),"No Data availble",Toast.LENGTH_SHORT).show()
                }else{

                    for(doc in it.documents){
                        val data = doc.toObject(CategoryModel::class.java)
                        list.add(data!!)
                    }

                    binding.categoryRecyclerview.adapter = CategoryAdapter(requireContext(),list)

                }

            }
    }

    private fun uploadimageurl(imageUrl: Uri,categoryText:String) {
        diaglog.show()
        val filename = UUID.randomUUID().toString()+".jpg"


        dbstorage.reference.child("category/$filename").putFile(imageUrl)

            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image->
                    storeData(image.toString(),categoryText)
                }
            }
            .addOnFailureListener{
                diaglog.dismiss()
                Toast.makeText(requireContext(),"Slider Uploaded Successfully", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(imageuri: String, categoryText: String) {
        val db = Firebase.firestore

        val data = hashMapOf<String,Any>(
            "img" to imageuri,
            "cateGory" to categoryText
        )
        db.collection("category").add(data)
            .addOnSuccessListener {
                diaglog.dismiss()
                Toast.makeText(requireContext(),"Category Uploaded Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                diaglog.dismiss()
                Toast.makeText(requireContext(),"error => ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var launchGallaryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl =  it.data!!.data
            binding.pickCategoryImage.setImageURI(imageUrl)

        }
    }
    var CamereResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data!!.extras!!["data"] as Bitmap?
            imageUrl = getimageurifrombitmap(requireContext(), bitmap)
            binding.pickCategoryImage.setImageURI(imageUrl)
        }
    }

    private fun getimageurifrombitmap(applicationContext: Context, bitmap: Bitmap?): Uri {
        val bytobj = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, bytobj)
        val path = MediaStore.Images.Media.insertImage(
            applicationContext.contentResolver,
            bitmap,
            "anything",
            "this  is something else"
        )
        return Uri.parse(path)
    }
}