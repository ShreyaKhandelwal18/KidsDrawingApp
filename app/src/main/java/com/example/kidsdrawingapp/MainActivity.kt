package com.example.kidsdrawingapp


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var drawingView : DrawingView? =null
    private var mImageBtnCurrentPaint: ImageButton? = null
     var customProgressDialog :Dialog?=null

    val openGalleryLauncher :ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode== RESULT_OK && result.data!=null){
                val imageBackGround :ImageView =findViewById(R.id.iv_background)

                imageBackGround.setImageURI(result.data?.data)
            }
        }


    val requestPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach {
                val permissionName =it.key
                val isGranted =it.value

                if(isGranted){
                    if(permissionName==Manifest.permission.READ_MEDIA_IMAGES){
                        Toast.makeText(this@MainActivity,"Permission Granted For  External Storage",Toast.LENGTH_LONG).show()
                        val pickIntent =Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    }
                }
                else{
                    if (permissionName==Manifest.permission.READ_MEDIA_IMAGES){
                        Toast.makeText(this@MainActivity,"Oops Permission denied for External Storage",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view)


        drawingView?.setSizeForBrush(20.toFloat())
         val ibBrush :ImageButton =findViewById(R.id.ib_brush)
        val ibGallery:ImageButton= findViewById(R.id.ib_gallery)
        val ibUndo :ImageButton = findViewById(R.id.ib_undo)
        val ibSave :ImageButton =findViewById(R.id.ib_save)
        val linearLayoutPaintColors =findViewById<LinearLayout>(R.id.ll_paint_colors)


        mImageBtnCurrentPaint = linearLayoutPaintColors[2] as ImageButton
        mImageBtnCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressedl)
        )

        ibBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }
        ibUndo.setOnClickListener {
          drawingView?.onClickUndo()
        }

        ibSave.setOnClickListener {
            if(isReadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch {
                     val flDrawingView :FrameLayout =findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }
    }

    private fun showBrushSizeChooserDialog(){
       val brushDialog= Dialog(this)
       brushDialog.setContentView(R.layout.dialog_brush_size)
       brushDialog.setTitle("Brush Size:")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)

        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View){
        if(view !=mImageBtnCurrentPaint){
            val imageButton =view as ImageButton
            val colorTag= imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressedl)
            )
            mImageBtnCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)

            )
            mImageBtnCurrentPaint=view

        }
    }

    private fun isReadStorageAllowed() :Boolean{
        var result =ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)

        return result == PackageManager.PERMISSION_GRANTED

    }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        )){
            showRationaleDialog("Kids Drawing App","Kids Drawing App "+"needs to Access your External Storage ")
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission.launch(arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
            }else{
                requestPermission.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }

    }


    private fun showRationaleDialog(title:String,message:String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){dialog,_->
                dialog.dismiss()

            }
        builder.create().show()
    }

private fun getBitmapFromView(view: View): Bitmap {
    val returnedBitmap =Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
    val canvas =Canvas(returnedBitmap)
    val bgDrawable = view.background
    if(bgDrawable !=null){
        bgDrawable.draw(canvas)
    }else{
        canvas.drawColor(Color.WHITE)
    }
    view.draw(canvas)
    return returnedBitmap
}


    @SuppressLint("SuspiciousIndentation")
    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result =""
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try{
                   val bytes =ByteArrayOutputStream()
                   mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

//                   val f= File(externalCacheDir?.absoluteFile.toString()+File.separator + "KidDrawingApp_" + System.currentTimeMillis())
                    val fileName = "KidDrawingApp_" + System.currentTimeMillis() + ".png"
                    val file = File(getExternalFilesDir(null), fileName)


                    val fo= FileOutputStream(file)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result= file.absolutePath


                    runOnUiThread{
                        cancelProgressDialog()
                        if (result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,"File saved successfully :$result",Toast.LENGTH_SHORT).show()
                            shareImage(result)
                        }else{
                            Toast.makeText(this@MainActivity,"Something went wrong on saving file ",Toast.LENGTH_LONG).show()
                        }
                    }
                }
                catch (e:Exception){
                 result= ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun showProgressDialog(){
         customProgressDialog = Dialog(this@MainActivity)

        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        customProgressDialog?.show()
    }

    private fun cancelProgressDialog(){
        if(customProgressDialog!=null){
            customProgressDialog?.dismiss()
            customProgressDialog=null

        }
    }

    private fun shareImage(result:String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path ,uri ->
            val shareIntent =Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type ="image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }
}
