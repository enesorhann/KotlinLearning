package com.enesorhan.landmarkbook

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesorhan.landmarkbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var landmarkList : ArrayList<Landmark>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        landmarkList = ArrayList<Landmark>()

        val pisa = Landmark("Pisa Tower","Italy",R.drawable.pisa)
        val colosseum = Landmark("Colosseum","Italy",R.drawable.colosseum)
        val londonBridge = Landmark("London Bridge","UK",R.drawable.londonbridge)
        val eiffel = Landmark("Eiffel Tower","French",R.drawable.eiffel)

        landmarkList.add(pisa)
        landmarkList.add(colosseum)
        landmarkList.add(londonBridge)
        landmarkList.add(eiffel)
/*
        val  adapter = ArrayAdapter(
            this@MainActivity,android.R.layout.simple_list_item_1,landmarkList.map { landmark -> landmark.name }
        )
        binding.listView.adapter = adapter
        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            var intent = Intent(this@MainActivity,DetailsPage::class.java)
            intent.putExtra("landmark",landmarkList.get(position))
            startActivity(intent)

        }
    */

        val recyclerAdapter = LandmarkAdapter(landmarkList)
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
    }
}