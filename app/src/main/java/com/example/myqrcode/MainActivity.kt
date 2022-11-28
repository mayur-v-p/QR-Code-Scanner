package com.example.myqrcode

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {

    private lateinit var btnCamera : MaterialButton
    private lateinit var btnGallery : MaterialButton
    private lateinit var btnScan : MaterialButton
    private lateinit var tvResults : TextView
    private lateinit var tvImage : ImageView

    companion object{
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
        private const val TAG = "MAIN_TAG"
    }

    private lateinit var cameraPermission : Array<String>
    private lateinit var storagePermission : Array<String>

    private var imageUri : Uri? = null

    private var barcodeScannerOption : BarcodeScannerOptions? = null
    private var barcodeScanner : BarcodeScanner? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnScan = findViewById(R.id.btnScan)
        tvImage = findViewById(R.id.tvImage)
        tvResults = findViewById(R.id.tvResults)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


        barcodeScannerOption = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOption!!)

        btnCamera.setOnClickListener {

            if (checkCameraPermission()){
                pickImageCamera()
            }else{
                requestCameraPermission()
            }

        }
        btnGallery.setOnClickListener {

            if (checkStoragePermission()){
                pickImageGallery()
            }else{
                requestStoragePermission()
            }

        }
        btnScan.setOnClickListener {

            if (imageUri == null){

                showToast("Please select image...")
            }else{
                detectResultFromImage()
            }

        }


    }

    private fun detectResultFromImage(){
        Log.d(TAG, "detectResultFromImage: ")
        try {

            val inputImage  = InputImage.fromFilePath(this, imageUri!!)
            val barcodeResult = barcodeScanner!!.process(inputImage)
                .addOnSuccessListener { barCodes->
                    exactBarcodeQrCodeInfo(barCodes)

                }
                .addOnFailureListener {e->
                    Log.d(TAG, "detectResultFromImage: " ,e)
                    showToast("Fail scanning Du To ${e.message}" )

                }

        }catch(e : Exception) {
            Log.d(TAG, "detectResultFromImage: ", e)
            showToast("Fail due to ${e.message}")

        }

    }

    private fun exactBarcodeQrCodeInfo(barcodes :List<Barcode>) {

        for (barcode in barcodes){
            val bounds = barcode.boundingBox
            val corners = barcode.cornerPoints
            val rawValue = barcode.rawValue

            Log.d(TAG, "exactBarcodeQrCodeInfo: rawValue: $rawValue")

            val valueType = barcode.valueType
            when(valueType){
                Barcode.TYPE_WIFI ->{
                    val typeWifi = barcode.wifi
                    val ssid = "${typeWifi?.ssid}"
                    val password = "${typeWifi?.password}"
                    var encryptionType = "${typeWifi?.encryptionType}"

                    if (encryptionType == "1"){
                        encryptionType = "OPEN"
                    }
                    else if (encryptionType == "2"){
                        encryptionType = "WPA"
                    }
                    else if (encryptionType == "3"){
                        encryptionType = "WEP"
                    }

                    Log.d(TAG, "exactBarcodeQrCodeInfo: TYPE_WIFI")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: ssid : $ssid")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: password: $password")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: encryption: $encryptionType")

                    tvResults.text = "TYPE_WIFI \nssid: $ssid \npassword: $password \nencryption: $encryptionType \n\nrawValue:$rawValue "
                }

                Barcode.TYPE_URL ->{

                    val typeUrl = barcode.url
                    val title = "${typeUrl?.title}"
                    val url = "${typeUrl?.url}"

                    Log.d(TAG, "exactBarcodeQrCodeInfo: TYPE_URL")
                    Log.d(TAG, "exactBarcodeQrCodeInfo:title:$title ")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: url:$url")

                    tvResults.text = "TYPE_URL \ntitle : $title\nurl: $url \n\nrawValue: $rawValue "

                }
                Barcode.TYPE_EMAIL ->{

                    val typeEmail = barcode.email
                    val address = "${typeEmail?.address}"
                    val body = "${typeEmail?.body}"
                    val subject = "${typeEmail?.subject}"

                    Log.d(TAG, "exactBarcodeQrCodeInfo: TYPE_MAIL")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: address: $address")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: body:$body")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: subject:$subject")

                    tvResults.text = "TYPE_EMAIL\n Email : $address\n body:$body\n subject:$subject\n\n rawValue : $rawValue"

                }
                Barcode.TYPE_CONTACT_INFO ->{

                    val typeContact = barcode.contactInfo

                    val title = "${typeContact?.title}"
                    val organization = "${typeContact?.organization}"
                    val name = "${typeContact?.name?.first} ${typeContact?.name?.last}"
                    val phone = "${typeContact?.name?.first} ${typeContact?.phones?.get(0)?.number}"

                    Log.d(TAG, "exactBarcodeQrCodeInfo: TYPE_CONTACT_INFO")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: title:$title")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: organization:$organization")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: name:$name")
                    Log.d(TAG, "exactBarcodeQrCodeInfo: phone:$phone")

                    tvResults.text = "TYPE_CONTACT_INFO\ntitle: $title\n organization:$organization \n name:$name\n phone:$phone \n\nrawValue: $rawValue"

                }
                else->{
                    tvResults.text = "rawValue : $rawValue"
                }
            }
        }

    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->

        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data?.data
            Log.d(TAG,"galleryActivityResultLauncher: imageUri : $imageUri")
            tvImage.setImageURI(imageUri)

        }
        else{
            showToast("Cancelled.....")
        }
    }


    private fun pickImageCamera(){

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Images")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Images Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->

        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            Log.d(TAG, "cameraActivityResultLauncher imageUri: $imageUri")

            tvImage.setImageURI(imageUri)
        }

    }

    private fun checkStoragePermission(): Boolean{

        val result = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        return result;

    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission():Boolean{
        val resultCamera = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        val resultStorage = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

        return resultCamera && resultStorage
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){

            CAMERA_REQUEST_CODE ->{

                if (grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }
                    else{
                        showToast("Camera and Storage permission are required !")
                    }
                }

            }

            STORAGE_REQUEST_CODE ->{

                if (grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted){
                        pickImageGallery()
                    }
                    else{
                        showToast("Storage permission is required ...")
                    }
                }

            }


        }
    }

    private fun showToast(message: String){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
}