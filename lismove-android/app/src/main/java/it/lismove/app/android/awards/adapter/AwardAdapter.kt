package it.lismove.app.android.awards.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.databinding.ItemAwardBinding

class AwardAdapter(
    var items: List<AwardItemUI>,
    var onItemClicked: (item: AwardItemUI)->Unit
): RecyclerView.Adapter<AwardAdapter.AwardViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AwardViewHolder {
        val binding = ItemAwardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AwardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AwardViewHolder, position: Int) {
        holder.bind(items[position],onItemClicked)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class AwardViewHolder(val binding: ItemAwardBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: AwardItemUI, onItemClicked: (item: AwardItemUI) -> Unit){
            val context = binding.root.context
            binding.awardTitle.text = item.name
            with(binding.awardLeftImage){

                if(item.image.isNullOrEmpty().not() && URLUtil.isValidUrl(item.image)){
                    load(item.image){
                        transformations(CircleCropTransformation())
                    }
                }else{
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)
                    load(avatarPic)
                }

            }

            with(binding.awardValue){
                isVisible = item.value.isNullOrEmpty().not()
                text = item.value
            }
            with(binding.awardLabel){
                isVisible = item.valueType.isNullOrEmpty().not()
                text = item.valueType
            }
            with(binding.rightIcon){
                isVisible = item.rightIcon != null
                item.rightIcon?.let { setImageDrawable(AppCompatResources.getDrawable(context, it)) }
                item.rightElementsColor?.let { imageTintList = AppCompatResources.getColorStateList(context, it)}
            }

            with(binding.awardHeader){
                isVisible = item.header.isNullOrEmpty().not()
                text = item.header
            }
            with(binding.rightText){
                isVisible = item.rightText != null
                text = item.rightText
                item.rightElementsColor?.let { setTextColor(AppCompatResources.getColorStateList(context, it))  }
            }

            binding.root.setOnClickListener {
                onItemClicked(item)
            }

        }


    }

}