package net.nextome.lismove_sdk.statusListener

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData

class BluetoothStatusListener(val context: Context) : LiveData<Boolean>() {

    companion object {
        fun isBluetoothEnabled(): Boolean {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return mBluetoothAdapter?.isEnabled ?: false
        }
    }

    private lateinit var broadCastReceiver: MyBroadcastReceiver

    override fun onActive() {
        super.onActive()
        broadCastReceiver = MyBroadcastReceiver()
        broadCastReceiver.register()
    }

    override fun onInactive() {
        super.onInactive()
        broadCastReceiver.unregister()
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        fun register() {
            val bAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bAdapter == null) {
                postValue(false)
                return
            }

            if (value == null ) {
                //First instance retrieve value from BluetoothAdapter
                postValue(bAdapter.isEnabled)
            } else {
                //Check if value BluetoothAdapter is same a value observed
                if (value != bAdapter.isEnabled) postValue(bAdapter.isEnabled)
            }

            val filter = IntentFilter()
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            try {
                context.registerReceiver(broadCastReceiver, filter)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        fun unregister() {
            //safeUnregister
            try {
                context.unregisterReceiver(broadCastReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {

                val previousState =
                        intent.getIntExtra(
                                BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                                BluetoothAdapter.ERROR
                        )
                val state =
                        intent.getIntExtra(
                                BluetoothAdapter.EXTRA_STATE,
                                BluetoothAdapter.ERROR)

                if (previousState == BluetoothAdapter.STATE_OFF || previousState == BluetoothAdapter.STATE_TURNING_OFF) {
                    if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                        postValue(true)
                    } else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                        postValue(false)
                    }
                }
            }
        }
    }

}