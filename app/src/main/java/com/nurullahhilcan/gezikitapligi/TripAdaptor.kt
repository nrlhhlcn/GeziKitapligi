package com.nurullahhilcan.gezikitapligi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nurullahhilcan.gezikitapligi.databinding.RecyclerRowBinding

class TripAdaptor(val artList:ArrayList<Trip>): RecyclerView.Adapter<TripAdaptor.TripHolder>()  {

    class TripHolder(val binding:RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripHolder {
        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TripHolder(binding)
    }
    override fun getItemCount(): Int {
        return  artList.size
    }
    override fun onBindViewHolder(holder: TripHolder, position: Int) {
       holder.binding.textView2.text="Ali"
        holder.binding.imageView4.setBackgroundResource(R.drawable.select_button)
        holder.itemView.setOnClickListener{
            val intent= Intent(holder.itemView.context,GeziEkle::class.java)

            holder.itemView.context.startActivity(intent)
        }
    }

}