package it.lismove.app.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import timber.log.Timber

class AutoCompleteActivityTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_complete)



    }
}