package com.enesorhan.catchthekenny

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.enesorhan.catchthekenny.databinding.ActivityMainBinding
import java.util.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var number = 0
    private var imageList =  ArrayList<ImageView>()
    private var runnable: Runnable = Runnable {}
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        imageList.add(binding.imageView)
        imageList.add(binding.imageView1)
        imageList.add(binding.imageView4)
        imageList.add(binding.imageView5)
        imageList.add(binding.imageView6)
        imageList.add(binding.imageView7)
        imageList.add(binding.imageView8)
        imageList.add(binding.imageView9)
        imageList.add(binding.imageView10)
        for (img in imageList){
            img.visibility = View.INVISIBLE
        }
        movementImage()
        countDownTimer()
    }
    fun countDownTimer(){
        object :CountDownTimer(15000,1000){
            override fun onTick(millisUntilFinished: Long) {
                binding.textView.text = "Time: ${millisUntilFinished/1000}"

            }

            override fun onFinish() {
                binding.textView.text = "Time: 0"
                dialog()
                handler.removeCallbacks(runnable)
                for (img in imageList){
                    img.visibility = View.INVISIBLE
                }
            }

        }.start()
    }
    fun imageClick(view: View){
        binding.textView2.text = "Score: ${++number}"
    }
    fun dialog(){
        var alert = AlertDialog.Builder(this@MainActivity)
        alert.setTitle("Game Over")
        alert.setMessage("Restart The Game")
        alert.setPositiveButton("Yes",DialogInterface.OnClickListener { dialog, which ->
            //Restart
            number=0
            countDownTimer()
            movementImage()
            binding.textView2.text = "Score: ${number}"
        })
        alert.setNegativeButton("No",DialogInterface.OnClickListener{dialog, which ->
            //Exit
            System.exit(0)
        })
        alert.create().show()
    }
    fun movementImage(){
        runnable = object : Runnable{
            override fun run() {
                for(img in imageList){
                    img.visibility = View.INVISIBLE     }
                var index = Random().nextInt(9)
                imageList[index].visibility= View.VISIBLE
                handler.postDelayed(runnable,500)
            }

        }
        handler.post(runnable)
    }
}