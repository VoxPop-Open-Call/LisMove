package it.lismove.app.android.initiative.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ItemAddressBinding
import it.lismove.app.android.databinding.ItemListAlertDataBinding
import it.lismove.app.android.databinding.ViewListAlertDialogBinding
import it.lismove.app.android.initiative.ui.data.AddressItemUI
import it.lismove.app.android.initiative.ui.data.ListAlertData
import kotlin.io.path.createTempDirectory


class ListAlertDialogAdapter(
    var items: List<ListAlertData>,
    private var onItemClick:(item: ListAlertData) -> Unit
): RecyclerView.Adapter<ListAlertDialogAdapter.ListAlertDialogHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAlertDialogHolder {
        val binding = ItemListAlertDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListAlertDialogHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ListAlertDialogHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class ListAlertDialogHolder(val binding: ItemListAlertDataBinding, val callback: (ListAlertData)->Unit):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListAlertData){
            with(binding){
                root.setOnClickListener { callback(item) }
                leftText.text = item.leftText
                rightText.text = item.rightText
                topText.isVisible = !item.topText.isNullOrEmpty()
                topText.text = item.topText
                bottomText.isVisible = item.bottomText.isNullOrEmpty().not()
                bottomText.text = item.bottomText
                if(item.imageRes != null){
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)
                    leftImage.isVisible = true
                    leftImage.load(avatarPic)
                }else if(item.imageUrl != null){
                    leftImage.isVisible = true
                    leftImage.load(item.imageUrl){
                        transformations(CircleCropTransformation())
                    }
                }else{
                    leftImage.isVisible = false
                }


            }
        }

    }
}
