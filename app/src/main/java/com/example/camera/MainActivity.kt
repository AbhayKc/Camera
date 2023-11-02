package com.example.camera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.camera.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageView: ImageView? = null
    private var bitmapImg: Bitmap? = null
    private var imageName: String = ""
    private lateinit var filePhoto: File
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.imageView

        binding.galleryBtn.setOnClickListener {
            openGallery()
        }
        binding.cameraBtn.setOnClickListener {
            openCamera()
        }
    }
    private fun openCamera() {
        imageName = System.currentTimeMillis().toString() + ".jpg"
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        filePhoto = File(directoryStorage, imageName)

        val providerFile = FileProvider.getUriForFile(this, "$packageName.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        cameraResultLauncher.launch(takePhotoIntent)
    }

    private var cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getPhotoFile(Uri.fromFile(filePhoto).toString())
        }
    }
    @Throws(Exception::class)
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(galleryIntent)
    }
    private var galleryResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pathName = result.data?.data.toString()
            getPhotoFile(pathName)
        }
    }
    private fun getPhotoFile(fileName: String) {
        bitmapImg = MediaStore.Images.Media.getBitmap(
            contentResolver, Uri.parse(fileName)
        )
        val orientation = ExifUtil.getExifOrientation(this, Uri.parse(fileName))
        bitmapImg = ExifUtil.rotateBitmap(bitmapImg!!, orientation)
        imageView?.setImageBitmap(bitmapImg)
    }
}
