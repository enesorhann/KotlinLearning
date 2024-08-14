package com.enesorhan.artbook
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enesorhan.artbook.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream



class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var db: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()

        db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("old")){
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",0)
            val cursor = db.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artName_Ix = cursor.getColumnIndex("artName")
            val artistName_Ix = cursor.getColumnIndex("artistName")
            val year_Ix = cursor.getColumnIndex("year")
            val image_Ix = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artText.setText(cursor.getString(artName_Ix))
                binding.artistText.setText(cursor.getString(artistName_Ix))
                binding.yearText.setText(cursor.getString(year_Ix))

                val byteArray = cursor.getBlob(image_Ix)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView2.setImageBitmap(bitmap)
            }
            cursor.close()
            //binding.artText.setText()
        }else{
            binding.button.visibility = View.VISIBLE
            binding.imageView2.setImageResource(R.drawable.gallery)
            binding.artText.setText(" ")
            binding.artistText.setText(" ")
            binding.yearText.setText(" ")
        }






    }

    fun imgClicked(view: View){

        /*
        if( VERSION.SDK_INT >= VERSION_CODES.TIRAMISU){ // 33 and +
            mediaPermission = "READ_MEDIA_IMAGES"

        }else{
            mediaPermission = "READ_EXTERNAL_STORAGE"
        }

         */

        if(ContextCompat.checkSelfPermission(this@ArtActivity,
                Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,Manifest.permission.READ_MEDIA_IMAGES)
                ){
                // Rationale
                Snackbar.make(
                    //  Request Permission
                    view, "Permission needed for Gallery",Snackbar.LENGTH_INDEFINITE).setAction(
                    "Give Permission",View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }).show()
            }else{
                //  Request Permission
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }

        }else{
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    fun saveButton(view: View){

        val artName = binding.artText.text.toString()
        val artistName = binding.artistText.text.toString()
        val yearText = binding.yearText.text.toString()

        if(selectedBitmap != null){
            var smallerBitmap = makeSmillarBitMap(selectedBitmap!!,300)
            var outputStream = ByteArrayOutputStream()
            smallerBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            var byteArray = outputStream.toByteArray() // Save format to binary

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS arts" +
                        " (id INTEGER PRIMARY KEY,artName VARCHAR,artistName VARCHAR,year VARCHAR,image BLOB)")

                var sqlQuery = "INSERT INTO arts(artName,artistName,year,image) VALUES (?,?,?,?)"
                var statement = db.compileStatement(sqlQuery)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,yearText)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch (e: Exception){
                e.printStackTrace()
            }

            var intent = Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }



    }

    private fun makeSmillarBitMap(image: Bitmap,maximumSize: Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitMapRatio : Double = width.toDouble() / height.toDouble()

        if(bitMapRatio > 1){

            width = maximumSize
            val scaledHeight = width / bitMapRatio
            height = scaledHeight.toInt()
        }else{
            height = maximumSize
            val scaledWidth = height * bitMapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                   val imageData = intentFromResult.data

                    if (imageData != null){
                        try {
                            if(VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@ArtActivity.contentResolver,imageData)
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                // Permission Granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                // Permission Denied
                Toast.makeText(this@ArtActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }
    }
}