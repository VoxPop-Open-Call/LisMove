package it.lismove.app.android.logWall.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.databinding.ItemLogWallBinding

class LogWallAdapter(
    var items: List<String>
): RecyclerView.Adapter<LogWallAdapter.LogWallViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogWallViewHolder {
        val binding = ItemLogWallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogWallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogWallViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class LogWallViewHolder(val binding: ItemLogWallBinding): RecyclerView.ViewHolder(binding.root){
            fun bind(item: String){
                with(binding.logWallItemText){
                    //val test = "<li><b><u>2021-12-06</u></b><br/>utente dell'iniziativa Nextome ha registrato una sessione in bicicletta di <b>12.13 km </b> guadagnando <b><font color='#00ff00'>0.34 €</font></b> e <b>121</b> punti iniziativa e <b>121</b> punti nazionali</li>"
                    val textHtml = HtmlCompat.fromHtml(item, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    text = textHtml
                    typeface = Typeface.MONOSPACE
                }
            }
    }
}