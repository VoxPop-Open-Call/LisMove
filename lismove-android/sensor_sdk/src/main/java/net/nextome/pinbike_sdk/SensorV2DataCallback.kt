package net.nextome.lismove_sdk

import android.bluetooth.BluetoothDevice
import net.nextome.lismove_sdk.models.LisMoveSensorHistoryElement
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback
import no.nordicsemi.android.ble.data.Data

abstract class SensorV2DataCallback : ProfileDataCallback {

    val receivedHistoryList = arrayListOf<LisMoveSensorHistoryElement>()

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        if (data.size() < 1) {
            onInvalidDataReceived(device, data)
            return
        }

        // Decode the new data
        if (data.value == null) {
            onInvalidDataReceived(device, data)
            return
        }

        if (data.value?.size == 15) {
            // reverse endianess
            val littleEndianArray = byteArrayOf(
                data.value!![0],
                data.value!![1],
                data.value!![3],
                data.value!![2],
                data.value!![5],
                data.value!![4],
                data.value!![9],
                data.value!![8],
                data.value!![7],
                data.value!![6],
                data.value!![13],
                data.value!![12],
                data.value!![11],
                data.value!![10],
                data.value!![14],
            )

            val littleEndianData = Data(littleEndianArray)

            // history type
            if (littleEndianData.value!![0] != (0xBD).toByte()) return
            if (littleEndianData.value!![1] != (0x0E).toByte()) return

            val startLap = littleEndianData.getIntValue(Data.FORMAT_UINT16, 2)
            val stopLap = littleEndianData.getIntValue(Data.FORMAT_UINT16, 4)

            val startingUtc = littleEndianData.getLongValue(Data.FORMAT_UINT32, 6)
            val endingUtc = littleEndianData.getLongValue(Data.FORMAT_UINT32, 10)

            val startingTimeInMillis = (startingUtc ?: 0) * 1000
            val endingTimeInMillis = (endingUtc ?: 0) * 1000
            receivedHistoryList.add(LisMoveSensorHistoryElement(startLap, stopLap, startingTimeInMillis, endingTimeInMillis))
        }

        if (receivedHistoryList.size == 3) {
            onHistoryReceived(listOf(*receivedHistoryList.toTypedArray()))
        }
    }

    abstract fun onHistoryReceived(historyData: List<LisMoveSensorHistoryElement>)
}