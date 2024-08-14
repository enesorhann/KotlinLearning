package com.enesorhan.landmarkbook

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.enesorhan.landmarkbook.databinding.ActivityDetailsPageBinding

class DetailsPage : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //val intent = intent
        //val selectedLandmark = intent.getSerializableExtra("landmark") as Landmark
        val selectedLandmark = Singleton.chosenLandmark

        selectedLandmark?.let {
            binding.textView.text = it.name
            binding.textView2.text = it.country
            binding.imageView.setImageResource(it.image)
        }



    }
}