package it.lismove.app.android.general

import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import it.lismove.app.android.R
import it.lismove.app.android.general.utils.dismissKeyboard
import timber.log.Timber

open class LisMoveFragment(@LayoutRes contentLayoutId : Int = 0) : Fragment(contentLayoutId){
    private val loadingDialog: SweetAlertDialog? by lazy {
        context?.let {
            SweetAlertDialog(it, SweetAlertDialog.PROGRESS_TYPE).apply {
                this.progressHelper.barColor =  AppCompatResources.getColorStateList(it, R.color.red_main).defaultColor
                this.setCancelable(false)
            }
        }

    }

    open fun showLoadingAlert(){
        Timber.d("showAlert")
        loadingDialog?.show()
    }

    open fun hideLoadingAlert(){
        Timber.d("hideLoadingAlert")
        loadingDialog?.dismiss()
    }
    fun showError(errorDescription: String?){
        view?.let {
            Timber.e(errorDescription)
            val errorSnackBar = Snackbar
                .make(it, errorDescription ?: "Si è verificato un errore", Snackbar.LENGTH_LONG)
            errorSnackBar.show()
        }

    }


    fun showAlertDialog(title: String?, message: String?, onConfirmCallback: ()-> Unit = {}){
        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(
                "ok"
            ) { dialog, id ->
                onConfirmCallback()
            }
            builder.show()
        }

    }

    fun showAlertDialog(title: String, message: String, showDismiss: Boolean = false, onClick: ()->Unit){
        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(
                "ok"
            ) { dialog, id ->
                onClick()
            }

            if(showDismiss){
                builder.setNegativeButton("Annulla"){dialog, id ->}
            }
            builder.show()
        }

    }


    fun dismissKeyboard(){
        activity?.currentFocus?.dismissKeyboard()
    }

}

