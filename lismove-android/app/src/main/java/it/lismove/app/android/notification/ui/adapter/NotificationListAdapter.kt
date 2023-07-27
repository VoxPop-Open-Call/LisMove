package it.lismove.app.android.notification.ui.adapter

import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ItemNotificationBinding
import it.lismove.app.android.notification.data.NotificationListItem

class NotificationListAdapter(
    var items: List<NotificationListItem>,
    val itemExpandedCallback: (NotificationListItem) -> Unit
): RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding, itemExpandedCallback)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class NotificationViewHolder(val binding: ItemNotificationBinding,
                            val itemExpandedCallback: (NotificationListItem) -> Unit):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationListItem){
            with(binding){
                root.setOnClickListener {
                    toggleLayout(item)
                }
                title.text = item.title

                val spannableRule =  SpannableString(item.body ?: "Nessun regolamento disponibile")
                Linkify.addLinks(spannableRule, Linkify.ALL)
                body.text= spannableRule
                body.movementMethod = LinkMovementMethod.getInstance()

                date.text = item.date
                item.imageUrl?.let {
                    image.load(it){
                        transformations(CircleCropTransformation())
                    }
                }

                seenImage.visibility = if(item.seen) View.VISIBLE else View.INVISIBLE
                if (item.isInitiallyOpen){

                    toggleLayout(item)

                }
            }
        }



        private fun toggleLayout(item: NotificationListItem){
            with(binding){
                expandableLayout.toggle()
                val icon = if(expandableLayout.isExpanded) R.drawable.ic_baseline_arrow_drop_up_24 else  R.drawable.ic_baseline_arrow_drop_down_24
                arrow.setImageDrawable(AppCompatResources.getDrawable(root.context, icon))
                itemExpandedCallback(item)
                if(binding.expandableLayout.isExpanded){
                    seenImage.visibility = View.INVISIBLE

                }
            }
        }
    }

}