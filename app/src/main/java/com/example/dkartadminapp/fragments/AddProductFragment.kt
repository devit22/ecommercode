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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dkartadminapp.R
import com.example.dkartadminapp.adapters.SelelctedImageAdapter
import com.example.dkartadminapp.databinding.FragmentAddProductBinding
import com.example.dkartadminapp.models.AddProductModel
import com.example.dkartadminapp.models.CategoryModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class AddProductFragment : Fragment(){
private lateinit var binding: FragmentAddProductBinding

private lateinit var list:ArrayList<Uri>
private lateinit var listimage:ArrayList<String>
private lateinit var adapter:SelelctedImageAdapter


private var coverImageUri:Uri? = null
    private lateinit var diaglog:Dialog
    private var coverImageUrl:String? = " "
    private lateinit var categorylist :ArrayList<String>
    private lateinit var dbstorage: FirebaseStorage
    val permisions:Array<String> = arrayOf(
        Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    val permissioncode = 140201
    private var i = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        dbstorage = FirebaseStorage.getInstance()
        list = ArrayList()
        listimage = ArrayList()

        diaglog = Dialog(requireContext())
        diaglog.setContentView(R.layout.progress_layout)
        diaglog.setCancelable(false)

           setProductCategory()

        adapter = SelelctedImageAdapter(list)
        binding.productImageRecyclerview.adapter = adapter


        binding.selectProductCoverImageBtn.setOnClickListener {

            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(permisions,permissioncode)
            }else{
                val popupMenu = PopupMenu(context,binding.selectProductCoverImageBtn)
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

        binding.selectProductMainImagesBtn.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(permisions,permissioncode)
            }else{
                val popupMenu = PopupMenu(context,binding.selectProductCoverImageBtn)
                popupMenu.inflate(R.menu.imagemenu)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.camerasourch ->{
                            val intent = Intent()

                            intent.action = MediaStore.ACTION_IMAGE_CAPTURE

                            CamereResultLauncherMain.launch(intent)
                        }
                        R.id.fromgallery ->{
                            val intent = Intent()
                            intent.action = Intent.ACTION_GET_CONTENT
                            intent.type = "image/*"

                            launchGallaryActivityMain.launch(intent)
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        }

        binding.uploadProductDetailBtn.setOnClickListener {
            validatedata()
        }
       return binding.root
    }

    private fun validatedata() {
        val productName = binding.addProductNameEditext.text.toString()
        val productDescription = binding.addProductDescriptionEditext.text.toString()
        val productMrp = binding.addProductMrpEditext.text.toString()
        val productSp = binding.addProductSpEditext.text.toString()

        val selectcategortext = binding.productCategoryDropdown.selectedItem.toString()

        if(TextUtils.isEmpty(productName)){
            binding.addProductNameEditext.requestFocus()
            binding.addProductNameEditext.error = "Empty"
        }else if(TextUtils.isEmpty(productDescription)){
            binding.addProductDescriptionEditext.requestFocus()
            binding.addProductDescriptionEditext.error = "Empty"
        }else if(TextUtils.isEmpty(productMrp)){
            binding.addProductMrpEditext.requestFocus()
            binding.addProductMrpEditext.error = "Empty"
        }else if(TextUtils.isEmpty(productSp)){
            binding.addProductSpEditext.requestFocus()
            binding.addProductSpEditext.error = "Empty"
        }else if(selectcategortext.equals("Select Category")){
            Toast.makeText(requireContext(),"please select category",Toast.LENGTH_SHORT).show()
        }else if(coverImageUri == null){
            Toast.makeText(requireContext(),"please select product cover Image",Toast.LENGTH_SHORT).show()
        }else if(list.size <1){
            Toast.makeText(requireContext(),"please select product main images Image",Toast.LENGTH_SHORT).show()
        } else{
            i=0
            diaglog.show()
            uploadedata(productName,productDescription,productMrp,productSp,selectcategortext,coverImageUri!!)
        }



    }

    private fun uploadedata(
        productName: String,
        productDescription: String,
        productMrp: String,
        productSp: String,
        selectcategortext: String,
        coverImageUri: Uri,
    ) {
        val filename = UUID.randomUUID().toString()+".jpg"


        dbstorage.reference.child("products/$filename").putFile(coverImageUri)

            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image->
                    coverImageUrl = image.toString()

                    uploadProductimage(productName,productDescription,productMrp,productSp,selectcategortext)
                }
            }
            .addOnFailureListener{
                diaglog.dismiss()
                Toast.makeText(requireContext(),"Cover Image Updation failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProductimage(
        productName: String,
        productDescription: String,
        productMrp: String,
        productSp: String,
        selectcategortext: String,
    ) {
        val filename = UUID.randomUUID().toString()+".jpg"


        dbstorage.reference.child("products/$filename").putFile(list[i])

            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image->
                   listimage.add(image.toString())
                   if(list.size == listimage.size){
                       storedata(productName,productDescription,productMrp,productSp,selectcategortext)
                   }else{
                      i+=1
                      uploadProductimage(productName,productDescription,productMrp,productSp,selectcategortext)
                   }

                }
            }
            .addOnFailureListener{
                diaglog.dismiss()
                Toast.makeText(requireContext(),"Cover Image Updation failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storedata(
        productName: String,
        productDescription: String,
        productMrp: String,
        productSp: String,
        selectcategortext: String
    ) {
        val db = Firebase.firestore.collection("products")
        val key = db.document().id


        val data = AddProductModel(
            productName,
            productDescription,
            coverImageUrl,
            selectcategortext,
            key,
            productMrp,
            productSp,
            listimage
        )

        db.document(key).set(data).addOnSuccessListener {
            diaglog.dismiss()
            Toast.makeText(requireContext(),"Product Added",Toast.LENGTH_SHORT).show()
            binding.addProductNameEditext.text = null
        }
            .addOnFailureListener {
                diaglog.dismiss()
                Toast.makeText(requireContext(),"error => ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private var launchGallaryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            coverImageUri =  it.data!!.data
            binding.addPrductCoverImagePreview.visibility = View.VISIBLE
            binding.addPrductCoverImagePreview.setImageURI(coverImageUri)

        }
    }


    var CamereResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data!!.extras!!["data"] as Bitmap?
            coverImageUri = getimageurifrombitmap(requireContext(), bitmap)
            binding.addPrductCoverImagePreview.visibility = View.VISIBLE
            binding.addPrductCoverImagePreview.setImageURI(coverImageUri)

        }
    }
    private var launchGallaryActivityMain = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){
            coverImageUri =  it.data!!.data
            adapter.notifyDataSetChanged()
            list.add(coverImageUri!!)

        }
    }
    var CamereResultLauncherMain = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data!!.extras!!["data"] as Bitmap?
            coverImageUri = getimageurifrombitmap(requireContext(), bitmap)
            adapter.notifyDataSetChanged()
           list.add(coverImageUri!!)
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

    private fun setProductCategory(){
        categorylist = ArrayList()

        Firebase.firestore.collection("category").get().addOnSuccessListener {
            categorylist.clear()

            for(doc in it.documents){
                val data = doc.toObject(CategoryModel::class.java)
                categorylist.add(data!!.cateGory!!)

            }
            categorylist.add(0,"Select Category")

            val arrayAdapter = ArrayAdapter(requireContext(),R.layout.category_spinner_layout,categorylist)
            binding.productCategoryDropdown.adapter = arrayAdapter

        }
    }
}