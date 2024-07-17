package com.example.kidsdrawingapp


import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var drawingView : DrawingView? =null
    private var mImageBtnCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view)

        drawingView?.setSizeForBrush(20.toFloat())
         val ib_brush :ImageButton =findViewById(R.id.ib_brush)
        val linearLayoutPaintColors =findViewById<LinearLayout>(R.id.ll_paint_colors)


        mImageBtnCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageBtnCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressedl)
        )

        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
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

}