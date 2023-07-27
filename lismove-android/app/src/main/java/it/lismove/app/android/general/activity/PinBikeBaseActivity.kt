package it.lismove.app.android.general.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import it.lismove.app.android.R
import it.lismove.app.android.general.utils.dismissKeyboard
import timber.log.Timber

private  const val PERMISSIONS_REQUEST_CODE = 1

abstract class LisMoveBaseActivity: AppCompatActivity() {
    private lateinit var loadingDialog: SweetAlertDialog

    private var PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    private var PERMISSION_BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    fun showError(errorDescription: String?, view: View){
        Timber.e(errorDescription)
        val errorSnackBar = Snackbar
            .make(view, errorDescription ?: "Si è verificato un errore", Snackbar.LENGTH_LONG)
        errorSnackBar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.progressHelper.barColor =  AppCompatResources.getColorStateList(this, R.color.red_main).defaultColor
        loadingDialog.setCancelable(false)
    }

    fun showLoadingAlert(){
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.show()
    }

    fun hideLoadingAlert(){
        loadingDialog.dismiss()
    }

    fun showSweetDialogError(message: String? = null,
                             onConfirmationClicked: ()->Unit = {}){
        loadingDialog.apply{
            titleText = "Si è verificato un errore"
            contentText = message
            changeAlertType(SweetAlertDialog.ERROR_TYPE)
            if(loadingDialog.isShowing.not()) show()
            setConfirmClickListener {
                it.dismissWithAnimation()
                onConfirmationClicked()
            }

        }
    }
    fun showSweetDialogSuccess(title: String,
                               message: String?,
                               onConfirmationClicked: ()->Unit = {}) {

        loadingDialog.apply {
            titleText = title
            contentText = message
            confirmText = "Ok"
            setConfirmClickListener {
                it.dismissWithAnimation()
                onConfirmationClicked()
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            if(loadingDialog.isShowing.not()) show()
        }

    }

     fun showAlertDialog(title: String, message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
                "ok"
        ) { dialog, id ->
        }
        builder.show()
    }

    fun showAlertDialog(title: String, message: String, onClick: ()->Unit){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            "ok"
        ) { dialog, id ->
            onClick()
        }
        builder.show()
    }

    fun showConfirmationAlertDialog(title: String, message: String, onConfirmed: () -> Unit){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            "sì"
        ) { dialog, id ->
            onConfirmed()
        }
        builder.setNegativeButton("no"){
            dialog, id ->
        }
        builder.show()
    }
     fun dismissKeyboard(){
        currentFocus?.let { it.dismissKeyboard() }
    }

}