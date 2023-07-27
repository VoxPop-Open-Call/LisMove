package it.lismove.app.android.general.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.databinding.ViewMenuEntryBinding
import it.lismove.app.android.general.adapter.data.SimpleItem

class SimpleStringAdapter(
    var items: List<SimpleItem>,
    val callback: (SimpleItem)->Unit
): RecyclerView.Adapter<SimpleStringAdapter.SimpleStringViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleStringViewHolder {
        val binding = ViewMenuEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimpleStringViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: SimpleStringViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class SimpleStringViewHolder(val binding: ViewMenuEntryBinding, val callback: (SimpleItem)->Unit):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SimpleItem){
            with(binding){
                leftImageView.visibility = View.GONE
                rightImageView.visibility = View.GONE
                rightTitle.visibility = View.GONE
                rightLabel.visibility = View.GONE
                menuTitle.text = item.data
                mainSubtitle.text = item.subtitle
                mainSubtitle.isVisible = item.subtitle.isNullOrEmpty().not()
                binding.root.setOnClickListener { callback(item) }
            }
        }

    }
}
