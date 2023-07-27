package it.lismove.app.android.general.lce

interface LceView<TYPE> {

    fun onLoading()

    fun onSuccess(data: TYPE)

    fun onError(throwable: Throwable)
}