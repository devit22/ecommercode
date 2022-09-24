package com.example.dkartadminapp.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dkartadminapp.R
import com.example.dkartadminapp.databinding.FragmentSliderBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding
    private  var imageUrl: Uri? = null

    private lateinit var diaglog:Dialog
private lateinit var dbstorage:FirebaseStorage
    val permisions:Array<String> = arrayOf(
        Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    val permissioncode = 140201;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSliderBinding.inflate(layoutInflater)
dbstorage = FirebaseStorage.getInstance()
        diaglog = Dialog(requireContext())
        diaglog.setContentView(R.layout.progress_layout)
        diaglog.setCancelable(false)


        binding.apply {
            pickSliderImage.setOnClickListener {

                if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permisions,permissioncode)
                }else{
                    var popupMenu = PopupMenu(context,pickSliderImage)
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

            addUploadSliderBtn.setOnClickListener {
             if(imageUrl != null){
               uploadimageurl(imageUrl!!)
             }else{
                 Toast.makeText(requireContext(),"Please select image",Toast.LENGTH_SHORT).show()
             }


            }
        }


        return binding.root
    }

    private fun uploadimageurl(imageUrl: Uri) {
        diaglog.show()
        val filename = UUID.randomUUID().toString()+".jpg"

       // val  refstorage = FirebaseStorage.getInstance().reference.child("slider/$filename")
dbstorage.reference.child("slider/$filename").putFile(imageUrl)

            .addOnSuccessListener {
it.storage.downloadUrl.addOnSuccessListener { image->
    storeData(image.toString())
}
            }
            .addOnFailureListener{
                diaglog.dismiss()
                Toast.makeText(requireContext(),"Slider Uploaded Successfully",Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(imageuri: String) {
       val db = Firebase.firestore

        val data = hashMapOf<String,Any>(
            "img" to imageuri
        )
        db.collection("slider").document("item").set(data)
            .addOnSuccessListener {
            diaglog.dismiss()
                Toast.makeText(requireContext(),"Slider Uploaded Successfully",Toast.LENGTH_SHORT).show()
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
            imageUrl =  it.data!!.data
            binding.pickSliderImage.setImageURI(imageUrl)

        }
    }
    var CamereResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data!!.extras!!["data"] as Bitmap?
            imageUrl = getimageurifrombitmap(requireContext(), bitmap)
            binding.pickSliderImage.setImageURI(imageUrl)
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