package it.lismove.app.android.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityMapsBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber


class MapsActivity : LisMoveBaseActivity() {

    companion object {
        val INTENT_SESSION_ID = "intent_session_id"

        fun getIntent(context: Context, sessionId: String?): Intent{
            return Intent(context, MapsActivity::class.java).apply {
                sessionId?.let { putExtra(INTENT_SESSION_ID, sessionId) }
            }
        }
    }
    private lateinit var binding: ActivityMapsBinding
    val viewModel: MapViewModel by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}