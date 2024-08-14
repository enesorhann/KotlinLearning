package com.enesorhan.landmarkbook
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesorhan.landmarkbook.databinding.RecycleViewBinding

class LandmarkAdapter(val landmarkList : ArrayList<Landmark>) : RecyclerView.Adapter<LandmarkAdapter.LandmarkHolder>() {

    class LandmarkHolder(val binding: RecycleViewBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandmarkHolder {
        val binding = RecycleViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LandmarkHolder(binding)
    }

    override fun getItemCount(): Int {
        return landmarkList.size
    }

    override fun onBindViewHolder(holder: LandmarkHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = landmarkList.get(position).name

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailsPage::class.java)
            //intent.putExtra("landmark",landmarkList.get(position))
            Singleton.chosenLandmark = landmarkList.get(position)
            holder.itemView.context.startActivity(intent)
        }


    }


}