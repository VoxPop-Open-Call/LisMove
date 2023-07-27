package it.lismove.app.android.authentication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.authentication.ui.data.CityItemUI
import it.lismove.app.android.databinding.ItemCityAdapterBinding

class CityRecyclerViewAdapter(private val items: List<CityItemUI>,
                              val callback: CityAdapterCallback):
        RecyclerView.Adapter<CityRecyclerViewAdapter.CityViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class CityViewHolder(val binding: ItemCityAdapterBinding, val callback: CityAdapterCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityItemUI){
            with(binding){
                cityName.text = item.name
                lismoveLogo.visibility = if(item.lismoveCity) View.VISIBLE else View.INVISIBLE
                root.setOnClickListener { callback.onCityClicked(item) }
            }
        }

    }
}

