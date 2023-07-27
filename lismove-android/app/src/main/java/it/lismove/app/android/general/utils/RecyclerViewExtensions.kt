package it.lismove.app.android.general.utils

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addLineDivider(){
    val itemDecoration =  DividerItemDecoration(
        context,
        LinearLayoutManager.VERTICAL
    )
    addItemDecoration(itemDecoration)
}