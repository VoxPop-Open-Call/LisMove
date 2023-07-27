package it.lismove.app.android.general.lce

import androidx.lifecycle.Observer


open class LceDispatcher<TYPE>(private val view: LceView<TYPE>) : Observer<Lce<TYPE>> {
    override fun onChanged(lce: Lce<TYPE>?) {
        when (lce) {
            is LceLoading -> view.onLoading()
            is LceSuccess -> view.onSuccess(lce.data)
            is LceError -> view.onError(lce.error)
        }
    }
}