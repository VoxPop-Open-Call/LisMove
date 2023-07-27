package it.lismove.app.android.initiative.ui.adapter

import android.text.SpannableString
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.databinding.ItemRegulationListBinding
import it.lismove.app.android.initiative.ui.data.RegulationListItem

class RegulationListDialogAdapter(
    var items: List<RegulationListItem>
): RecyclerView.Adapter<RegulationListDialogAdapter.RegulationListDialogHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegulationListDialogHolder{
        val binding = ItemRegulationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegulationListDialogHolder(binding)
    }

    override fun onBindViewHolder(holder:RegulationListDialogHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    class RegulationListDialogHolder(val binding: ItemRegulationListBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RegulationListItem){
            with(binding){
                organizationTitle.text = item.title
                val spannableRule =  SpannableString(item.regulation)
                Linkify.addLinks(spannableRule, Linkify.ALL)

                with(binding.regulationDescription){
                    text = spannableRule
                    movementMethod = android.text.method.LinkMovementMethod.getInstance()
                }
            }
        }

    }
}
