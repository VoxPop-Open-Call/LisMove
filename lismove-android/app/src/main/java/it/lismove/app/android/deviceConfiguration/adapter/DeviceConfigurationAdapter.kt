package it.lismove.app.android.deviceConfiguration.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.lismove.app.android.deviceConfiguration.DeviceConfigCallback
import it.lismove.app.android.deviceConfiguration.DeviceConfigurationFragment
import timber.log.Timber

class DeviceConfigurationAdapter(activity: AppCompatActivity,
                                 private val callback: DeviceConfigCallback,
                                 private val itemsCount: Int) :
        FragmentStateAdapter(activity) {
    var error: String? = null
    override fun getItemCount(): Int {
        return itemsCount
    }



    override fun createFragment(position: Int): Fragment {
        Timber.d("createFragment with $position and $error")
        if(position == 3 && error.isNullOrBlank().not()){
            val fragment =  DeviceConfigurationFragment(position+1, callback)
             fragment.setError(error ?: "")
            return fragment
        }
        return DeviceConfigurationFragment(position, callback)
    }



}