package it.lismove.app.android.session.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.databinding.ItemSessionBinding
import it.lismove.app.android.session.ui.data.SessionListItemUI

class SessionListAdapter(
        var items: List<SessionListItemUI>,
        val itemClickedCallback: (SessionListItemUI) -> Unit
): RecyclerView.Adapter<SessionListAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding, itemClickedCallback)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class SessionViewHolder(val binding: ItemSessionBinding,
                            val itemClickedCallback: (SessionListItemUI) -> Unit):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SessionListItemUI){
            with(binding){
                root.setOnClickListener { itemClickedCallback(item) }
                sessionItemDate.text = item.date
                sessionItemDistance.text = item.distance
                sessionItemInitiativePoints.text = item.initiativePoint
                sessionItemNationalPoins.text = item.nationalPoint
                sessionItemRoundIcon.visibility = if(item.showRoundAlert) View.VISIBLE else View.INVISIBLE

            }
        }

    }
}