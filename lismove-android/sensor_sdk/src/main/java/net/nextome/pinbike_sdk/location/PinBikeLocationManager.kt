package net.nextome.lismove_sdk.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import net.nextome.lismove_sdk.models.LisMoveGpsPosition
import net.nextome.lismove_sdk.utils.BugsnagUtils
import net.nextome.lismove_sdk.utils.SessionDelayManager
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

private const val UPDATE_MIN_DISTANCE = 0F
private const val REFRESH_THRESHOLD_IN_SECONDS = 60*5 // 5 minutes
private const val TAG = "LisMoveLocation"

class LisMoveLocationManager(context: Context) {
    private val PARTIAL_WRITE_RATE = SessionDelayManager.getDelay(context).toLong() * 1000

    // GPS partials are collected 1 second more frequent than partial write rate
    private val UPDATE_MIN_TIME = if (PARTIAL_WRITE_RATE <= 1L) 1 else { PARTIAL_WRITE_RATE - 1 }
    private var isLocationActive = true

    private val gpsLocationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val passiveLocationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val towerLocationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isLocationEnabled(context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    } else {
        try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    // Values for debug purposes
    var lastReceivedProviderGps: Location? = null
    var lastReceivedProviderPassive: Location? = null
    var lastReceivedProviderNetwork: Location? = null

    @SuppressLint("MissingPermission")
    /**
     * Get most recent user location.
     * Location may be cached or new, based on how old it is.
     *
     * @param forceNew: if true, never returns a cached position (may take a looooot of time to converge)
     */
    suspend fun awaitLastLocationOrNull(applicationContext: Context, forceNew: Boolean = false) = suspendCancellableCoroutine<LisMoveGpsPosition?> { continuation ->
        Timber.tag(TAG).i("New location request with forceNew: %s", forceNew)

        CoroutineScope(continuation.context).launch {
            // Try to get recently cached position
            if (!forceNew) {
                getLastPositionOrNull()?.let {

                    val timeSinceBoot = SystemClock.elapsedRealtimeNanos()
                    val differenceInSeconds = (timeSinceBoot - it.elapsedTimeNanos) / 1000000000

                    if (differenceInSeconds < REFRESH_THRESHOLD_IN_SECONDS) {
                        continuation.resume(it) {}

                        Timber.tag(TAG).i("Got recently computed position (" + it?.latitude + ", " + it?.longitude + ")")
                        return@launch
                    } else {
                        Timber.tag(TAG).i("Cached position was too old.")
                        Timber.tag(TAG).i("Requesting a fresh one.")
                        // position is too old, proceed refreshing
                    }
                }
            }

            // Try current GPS first, then network and passive
            getCurrentLocation(LocationManager.GPS_PROVIDER, applicationContext)?.let {
                Log.i(TAG, "Got fresh GPS location.")

                continuation.resume(it) {}
                return@launch
            }

            getCurrentLocation(LocationManager.NETWORK_PROVIDER, applicationContext)?.let {
                Timber.tag(TAG).i("Got fresh NETWORK location.")

                continuation.resume(it) {}
                return@launch
            }

            getCurrentLocation(LocationManager.PASSIVE_PROVIDER, applicationContext)?.let {
                Log.i(TAG, "Got fresh PASSIVE location.")

                continuation.resume(it) {}
                return@launch
            }

            getCurrentLocation("FUSED_PROVIDER", applicationContext)?.let {
                Timber.tag(TAG).i("Got fresh FUSED location.")

                continuation.resume(it) {}
                return@launch
            }

            if (forceNew) {
                Timber.tag(TAG).i("No fresh location were available.")
                // no new position available
                continuation.resume(null) {}
            } else {
                Timber.tag(TAG).i("No fresh location were available, returning and old one.")

                // return an old position
                continuation.resume(getLastPositionOrNull()) {}
            }
        }
    }

    private val DEFAULT_LOCATION_UPDATE_INTERVAL = SessionDelayManager.getDelay(context) * 1000L
    var currentGpsLocation: Location? = null
    var currentNetworkLocation: Location? = null
    var currentPassiveLocation: Location? = null

    private val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(it: Location) {
            Timber.tag("LocationLogs").e("Got new GPS location (" + it.latitude + ", " + it.longitude + ")")
            currentGpsLocation = it

            // Log for debug purposes
            lastReceivedProviderGps = it
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val networkLocationListener = object : LocationListener {
        override fun onLocationChanged(it: Location) {
            Timber.tag("LocationLogs").e("Got new Network location (" + it.latitude + ", " + it.longitude + ")")
            currentNetworkLocation = it

            // Log for debug purposes
            lastReceivedProviderNetwork = it
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val passiveLocationListener = object : LocationListener {
        override fun onLocationChanged(it: Location) {
            Timber.tag("LocationLogs").e("Got new Passive location (" + it.latitude + ", " + it.longitude + ")")
            currentPassiveLocation = it

            // Log for debug purposes
            lastReceivedProviderPassive = it
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun locationObservable(currentCoroutineContext: CoroutineContext, applicationContext: Context, interval: Long = DEFAULT_LOCATION_UPDATE_INTERVAL) = callbackFlow<LisMoveGpsPosition?> {
        forceXtraTimeInjection()

        addUpdateListeners()

        CoroutineScope(currentCoroutineContext).launch {
            while (true) {
                if (isLocationActive) {
                    Timber.tag(TAG).d("Location Active")
                    Timber.tag(TAG).d("GPS TICK STATUS")
                    Timber.tag(TAG).d( "GPS: (${currentGpsLocation?.latitude}, ${currentGpsLocation?.longitude})")
                    Timber.tag(TAG).d( "NETWORK (${currentNetworkLocation?.latitude}, ${currentNetworkLocation?.longitude})")
                    Timber.tag(TAG).d("PASSIVE (${currentPassiveLocation?.latitude}, ${currentPassiveLocation?.longitude})")
                    Timber.tag(TAG).d( "---------------")

                    if (currentGpsLocation != null) {
                        trySend(currentGpsLocation?.asLisMoveGpsPosition())
                        Timber.tag("gpsTest").e("GPS CHOSEN")
                    } else {
                        if (currentNetworkLocation != null) {
                            trySend(currentNetworkLocation?.asLisMoveGpsPosition())
                            Timber.tag("gpsTest").e("NETWORK CHOSEN")
                        } else {
                            trySend(currentPassiveLocation?.asLisMoveGpsPosition())
                            Timber.tag("gpsTest").e("PASSIVE CHOSEN")
                        }
                    }

                    currentGpsLocation = null
                    currentNetworkLocation = null
                    currentPassiveLocation = null

                }

                delay(interval)
            }
        }

        CoroutineScope(currentCoroutineContext).launch {
            while (true) {
                checkServiceHealthStatus()
                delay(150000L)
            }
        }

        awaitClose {
            gpsLocationManager.removeUpdates(gpsLocationListener)
            towerLocationManager.removeUpdates(networkLocationListener)
            passiveLocationManager.removeUpdates(passiveLocationListener)
        }

    }.onStart { emit(awaitLastLocationOrNull(applicationContext)) }


    /**
     * Store gps position each 2 minutes.
     * If position is still the same, perform GPS force reset.
     */
    var healthCheckGpsPosition: Location? = null
    private suspend fun checkServiceHealthStatus() {
        if (isLocationActive) {
            if (healthCheckGpsPosition == null) {
                healthCheckGpsPosition = lastReceivedProviderGps
                return
            }

            if (areLocationEquals(healthCheckGpsPosition, lastReceivedProviderGps)) {
                // Position is 2 mins old, perform gps reset
                tryRecoverGps()
            }

            healthCheckGpsPosition = lastReceivedProviderGps
        }
    }

    var areListenerAdded = false
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun addUpdateListeners() {
        if (!areListenerAdded) {
            withContext(Dispatchers.Default) {
                gpsLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE,
                    gpsLocationListener, Looper.getMainLooper()
                )

                towerLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE,
                    networkLocationListener, Looper.getMainLooper()
                )

                passiveLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE,
                    passiveLocationListener, Looper.getMainLooper()
                )
            }

            Timber.tag("LocationLogs").e("Added update listeners")
            areListenerAdded = true
        }
    }

    private suspend fun removeUpdateListeners() {
        if (areListenerAdded) {
            withContext(Dispatchers.Default) {
                gpsLocationManager.removeUpdates(gpsLocationListener)
                towerLocationManager.removeUpdates(networkLocationListener)
                passiveLocationManager.removeUpdates(passiveLocationListener)
            }

            areListenerAdded = false

            Timber.tag("LocationLogs").e("Removed update listeners")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun resumeLocation() {
        addUpdateListeners()
        isLocationActive = true
    }

    suspend fun pauseLocation() {
        removeUpdateListeners()
        isLocationActive = false
    }

    fun getDebugInfo() = LisMoveLocationDebug(
        lastReceivedProviderGps = lastReceivedProviderGps?.asLisMoveGpsPosition(),
        lastReceivedProviderPassive = lastReceivedProviderPassive?.asLisMoveGpsPosition(),
        lastReceivedProviderNetwork =lastReceivedProviderNetwork?.asLisMoveGpsPosition(),
    )

    /**
     * Try to get most recent location from GPS, NETWORK or PASSIVE in this order.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(provider: String, applicationContext: Context) = suspendCancellableCoroutine<LisMoveGpsPosition?> { continuation ->
        when (provider) {
            LocationManager.GPS_PROVIDER -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        gpsLocationManager.getCurrentLocation(provider, null,
                            ContextCompat.getMainExecutor(applicationContext), {
                                continuation.resume(it?.asLisMoveGpsPosition()) {}
                            }
                        )
                    }else {
                        Timber.tag(TAG).e("Androd version < R returning null position")
                        continuation.resume(null) {}
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e("Gps Provider not available")
                    continuation.resume(null) {}
                }
            }

            LocationManager.NETWORK_PROVIDER -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        towerLocationManager.getCurrentLocation(provider, null,
                            ContextCompat.getMainExecutor(applicationContext), {
                                continuation.resume(it?.asLisMoveGpsPosition()) {}
                            })
                    }else {
                        Timber.tag(TAG).e("Androd version < R returning null")
                        continuation.resume(null) {}

                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e("Gps Provider not available")
                    continuation.resume(null) {}
                }
            }

            LocationManager.PASSIVE_PROVIDER -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        passiveLocationManager.getCurrentLocation(provider, null,
                            ContextCompat.getMainExecutor(applicationContext), {
                                    continuation.resume(it?.asLisMoveGpsPosition()) {}
                            })
                    }else {
                        Timber.tag(TAG).e("Androd version < R returning null")
                        continuation.resume(null) {}
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e("Gps Provider not available")
                    continuation.resume(null) {}
                }
            }

            else -> {
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object: CancellationToken(){
                    override fun isCancellationRequested(): Boolean { return false }
                    override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {return CancellationTokenSource().token}
                }).addOnCompleteListener {
                    if (it?.isComplete) {
                        continuation.resume(it?.result?.asLisMoveGpsPosition()){}
                    }
                }
            }
        }
    }

    /**
     * Try to get Last position known to the system
     */
    @SuppressLint("MissingPermission")
    private suspend fun getLastPositionOrNull() = suspendCancellableCoroutine<LisMoveGpsPosition?> { continuation ->
        var hasResumed = false;

        val gpsLocation = gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.asLisMoveGpsPosition()
        if (gpsLocation != null) {
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(gpsLocation) {}
            }
        }

        val networkLocation = towerLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.asLisMoveGpsPosition()
        if (networkLocation != null) {
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(networkLocation) {}
            }
        }

        if (!hasResumed) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (!hasResumed) {
                    hasResumed = true
                    continuation.resume(it?.asLisMoveGpsPosition()) {}
                }
            }
        }

        val passiveLocation = passiveLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)?.asLisMoveGpsPosition()
        if (passiveLocation != null) {
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(passiveLocation) {}
            }
        }
    }

    /**
     * force_time_injection forces time assistance into gps software.
     * The time must be in GPS format. This is done by using NTP to determine a reasonably
     * accurate value for GPS time and give that to gps software in your phone
     * to use as assistance in establishing the initial position.
     *
     * Time assistance is only used for the first calculated position.
     * After the first position, the GPS receiver knows time to much greater accuracy
     * than you could ever provide from an external software interface.
     */
    private fun forceXtraTimeInjection(){
        try {
            val bundle = Bundle()

            gpsLocationManager.sendExtraCommand("gps", "force_xtra_injection", bundle)
            gpsLocationManager.sendExtraCommand("gps", "force_time_injection", bundle)
        } catch (e: Exception) {
            // can't perform gps optimization
            Timber.e(e)
        }
    }

    private fun deleteAidingData(){
        val bundle = Bundle()
        gpsLocationManager.sendExtraCommand("gps", "delete_aiding_data", bundle)
    }

    private suspend fun tryRecoverGps() {
/*        with ("Trying restoring GPS since position was the same for 2 mins") {
            BugsnagUtils.reportIssue(Exception(this))
            Timber.e(this)
        }*/

        try {
            pauseLocation()
            resumeLocation()

            deleteAidingData()
            forceXtraTimeInjection()
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            Timber.e(e)
        }
    }


    private fun Location.asLisMoveGpsPosition() = LisMoveGpsPosition(
        latitude, longitude, altitude, elapsedRealtimeNanos,
    )

    private fun areLocationEquals(start: Location?, end: Location?): Boolean {
        if (start == null || end == null) { return false }

        return (start.latitude == end.latitude) && (start.longitude == end.longitude)
    }
}