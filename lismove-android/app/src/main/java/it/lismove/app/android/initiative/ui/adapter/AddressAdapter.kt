package it.lismove.app.android.initiative.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.databinding.ItemAddressBinding
import it.lismove.app.android.initiative.ui.data.AddressItemUI


class AddressAdapter(var items: List<AddressItemUI>,
                     val callback: (AddressItemUI)->Unit
): RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class AddressViewHolder(val binding: ItemAddressBinding, val callback: (AddressItemUI)->Unit):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AddressItemUI){
            with(binding){
                addressName.text = item.name
                address.text = item.address
                itemLayout.setOnClickListener { callback(item) }

            }
        }

    }
}
