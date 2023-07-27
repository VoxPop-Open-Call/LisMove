package it.lismove.app.android.general

import android.app.Application
import com.bugsnag.android.Bugsnag
import it.lismove.app.android.general.network.NetworkKoinModule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.bugsnag.android.Configuration
import com.google.android.libraries.places.api.Places
import com.zoho.salesiqembed.ZohoSalesIQ
import it.lismove.app.android.BuildConfig
import it.lismove.app.common.PreferencesKeys.PREF_IS_APP_FOREGROUND
import it.lismove.app.android.R
import it.lismove.app.android.general.network.NetworkConfig.ZOHO_ACCESS_KEY
import it.lismove.app.android.general.network.NetworkConfig.ZOHO_APP_KEY
import it.lismove.app.android.general.utils.CoilImageLoader
import it.lismove.app.common.CommonKoinModule
import it.lismove.app.room.di.RoomKoinModule
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lv.chi.photopicker.ChiliPhotoPicker
import net.nextome.lismove_sdk.di.SdkKoinModule
import net.nextome.lismove_sdk.utils.BluetoothMedic
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber



const val BUILD_VARIANT_DEV = "dev"
const val BUILD_VARIANT_BETA = "beta"
const val BUILD_VARIANT_PROD = "prod"

const val IS_FORMIGGINI_GYRO_ONLY = false

class LisMoveApplication: Application(), LifecycleObserver {

    companion object {
        fun isProduction() = BuildConfig.FLAVOR == BUILD_VARIANT_PROD
        fun isFormiggini() = false
    }

    val datastore: DataStore<Preferences> by inject()
    val tempPrefsRepository: TempPrefsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        /*if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }*/


        configureZoho()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Timber.plant(Timber.DebugTree())
        ChiliPhotoPicker.init(
            loader = CoilImageLoader(),
            authority = this.packageName + ".fileprovider"
        )
        Places.initialize(applicationContext, getString(R.string.google_maps))
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        configureBugsnag()

        startKoin {
            androidContext(this@LisMoveApplication)
            modules(
                CommonKoinModule.getModule(),
                RoomKoinModule.getModule(),
                SdkKoinModule.getModule(),
                KoinModule.getModule(),
                NetworkKoinModule.getModule())

            with(tempPrefsRepository) {
                setConfigSent(false)
                setSessionFloatingOpen(false)
                setOptimizationSent(false)
            }
        }

        BluetoothMedic.getInstance().enablePowerCycleOnFailures(applicationContext)
    }

    private fun configureZoho(){
        ZohoSalesIQ.init(this, ZOHO_APP_KEY, ZOHO_ACCESS_KEY)
        ZohoSalesIQ.showLauncher(false)
    }

    private fun configureBugsnag() {
        val config = Configuration.load(this)

        with (config) {
            enabledReleaseStages = setOf(BUILD_VARIANT_BETA, BUILD_VARIANT_PROD)
            releaseStage = BuildConfig.VARIANT
        }

        Bugsnag.start(this, config)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        GlobalScope.launch { datastore.edit { it[PREF_IS_APP_FOREGROUND] = false } }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        GlobalScope.launch { datastore.edit { it[PREF_IS_APP_FOREGROUND] = true } }
    }
}