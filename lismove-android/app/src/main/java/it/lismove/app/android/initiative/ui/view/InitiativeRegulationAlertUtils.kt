package it.lismove.app.android.initiative.ui.view

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.databinding.DialogInitiativeRegulationBinding
import it.lismove.app.android.databinding.DialogInitiativeRegulationListBinding
import it.lismove.app.android.general.utils.addLineDivider
import it.lismove.app.android.initiative.ui.MyInitiativeActivity
import it.lismove.app.android.initiative.ui.adapter.RegulationListDialogAdapter
import it.lismove.app.android.initiative.ui.data.RegulationListItem

class InitiativeRegulationAlertUtils {
    companion object{
        fun getAlertDialog(ctx: Context, regulation: String?, organizationName: String): AlertDialog{

            val binding = DialogInitiativeRegulationBinding.inflate(LayoutInflater.from(ctx))

            with(binding.header){
                val first = "Per consultare i periodi di validità di ogni singola iniziativa, navigare nell'app, menù "
                val next = "\"<font color='#d80011'><u>Altro ➔ Gestione iniziative</u></font>\""
                text = HtmlCompat.fromHtml(first + next, HtmlCompat.FROM_HTML_MODE_COMPACT)
                setOnClickListener {
                    ctx.startActivity(Intent(ctx, MyInitiativeActivity::class.java))
                }
            }

            val spannableRule =  SpannableString(regulation ?: "Nessun regolamento disponibile")
            Linkify.addLinks(spannableRule, Linkify.ALL)

            with(binding.regulation){
                text = spannableRule
                movementMethod = LinkMovementMethod.getInstance()
            }


            val builder = AlertDialog.Builder(ctx)
            builder.setTitle(organizationName)
            builder.setView(binding.root)
            builder.setPositiveButton("chiudi") { dialog, which -> }
            return builder.create()
        }

        fun getAlertDialogList(ctx: Context, regulation: List<RegulationListItem>): AlertDialog{

            val binding = DialogInitiativeRegulationListBinding.inflate(LayoutInflater.from(ctx))

            with(binding.header){
                val first = "Per consultare i periodi di validità di ogni singola iniziativa, navigare nell'app, menù "
                val next = "\"<font color='#d80011'><u>Altro ➔ Gestione iniziative</u></font>\""
                text = HtmlCompat.fromHtml(first + next, HtmlCompat.FROM_HTML_MODE_COMPACT)
                setOnClickListener {
                    ctx.startActivity(Intent(ctx, MyInitiativeActivity::class.java))
                }
            }

            with(binding.recyclerView){
                layoutManager = LinearLayoutManager(ctx)
                adapter  = RegulationListDialogAdapter(regulation)
                addLineDivider()
            }

            val builder = AlertDialog.Builder(ctx)
            builder.setTitle("Bonus attivi")
            builder.setView(binding.root)
            builder.setPositiveButton("chiudi") { dialog, which -> }
            return builder.create()
        }

    }
}