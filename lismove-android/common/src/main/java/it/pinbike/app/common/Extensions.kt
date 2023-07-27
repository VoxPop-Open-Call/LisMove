package it.lismove.app.android.general.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.hadilq.liveevent.LiveEvent

fun Any.toJson() = Gson().toJson(this)
inline fun <reified T> String.fromJson() = Gson().fromJson(this, T::class.java)


fun View.dismissKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = LiveEvent<T>()
    result.addSource(this) {
        result.value = it
    }
    return result
}
