package it.lismove.app.android.initiative.ui.view

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.databinding.ViewListAlertDialogBinding
import it.lismove.app.android.initiative.ui.adapter.ListAlertDialogAdapter
import it.lismove.app.android.initiative.ui.data.ListAlertData

class ListAlertDialog {

    private lateinit var adapter: ListAlertDialogAdapter
    private lateinit var dialog: AlertDialog
    private lateinit var binding: ViewListAlertDialogBinding

    companion object{
        fun build(context: Context, title: String?,onClickListener: (ListAlertData, AlertDialog) -> Unit): ListAlertDialog{
            val binding = ViewListAlertDialogBinding.inflate(
                LayoutInflater.from(context))

            val equipmentDialog = ListAlertDialog()
            equipmentDialog.binding = binding
            val builder = AlertDialog.Builder(context)
            builder.setTitle(" ")


            builder.setView(binding.root)
            val dialog: AlertDialog = builder.create()
            val adapter = ListAlertDialogAdapter(listOf()){
                dialog.hide()
                onClickListener(it, dialog)
            }

            val itemDecoration =  DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )

            with(binding){
                alertRecycler.layoutManager = LinearLayoutManager(context)
                alertRecycler.adapter = adapter
                alertRecycler.addItemDecoration(itemDecoration)
            }

            dialog.setTitle(title)

            equipmentDialog.adapter = adapter
            equipmentDialog.dialog = dialog
            return equipmentDialog
        }
    }


    fun setData(data: List<ListAlertData>){
        binding.alertDialogLoading.visibility = View.GONE
        adapter.items = data
        adapter.notifyDataSetChanged()
    }
    /*
    fun update(data: SimpleListDialogUi){
        dialog.setTitle(data.title)
        subtitleTextView.text = data.subtitle
        subtitleTextView.isVisible = !data.subtitle.isNullOrEmpty()
        adapter.items = data.items
        adapter.notifyDataSetChanged()
        dialog.alertDialogLoading.visibility = View.GONE

    }*/



    fun show(){
        dialog.show()
    }

    fun showLoading(){
        binding.alertDialogLoading.visibility = View.VISIBLE

    }
    fun dismiss(){
        dialog.dismiss()
    }

    fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener) {
        dialog.setOnCancelListener(onCancelListener)
    }




}