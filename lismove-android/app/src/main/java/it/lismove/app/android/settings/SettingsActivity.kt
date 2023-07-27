package it.lismove.app.android.settings

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivitySettingsBinding
import net.nextome.lismove_sdk.utils.SessionDelayManager
import org.koin.android.ext.android.inject
import timber.log.Timber


class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding
    val viewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupThemeSwitch()
        // setupDelayLayout()
        setupBackgroundScanAutomaticStart()
        setupBackgroundScan()
    }

    private fun setupBackgroundScanAutomaticStart() {
        // Android 12
        if (Build.VERSION.SDK_INT < 31) {
            binding.backgroundScanAutomaticEnabledLayout.isVisible = true

            viewModel.backgroundSensorDetectionAutoStartEnabledObservable().observe(this) {
                binding.backgroundScanAutomaticEnabled.isChecked = it
            }

            binding.backgroundScanAutomaticEnabled.setOnClickListener {
                if (binding.backgroundScanAutomaticEnabled.isChecked) {
                    viewModel.setBackgroundSensorDetectionAutoStartEnabled(true, this)
                } else {
                    viewModel.setBackgroundSensorDetectionAutoStartEnabled(false, this)
                }
            }

        } else {
            viewModel.setBackgroundSensorDetectionAutoStartEnabled(false, this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupBackgroundScan() {
        viewModel.backgroundSensorDetectionEnabledObservable().observe(this) { enabled ->
            binding.backgroundScanEnabled.isChecked = enabled
        }

        binding.backgroundScanEnabled.setOnClickListener {
            if (binding.backgroundScanEnabled.isChecked) {
                showBackgroundScanDisclaimer()
                with (binding.backgroundScanAutomaticEnabled) {
                    isEnabled = true
                }
            } else {
                with (binding.backgroundScanAutomaticEnabled) {
                    isChecked = false
                    isEnabled = false
                }

                viewModel.setBackgroundSensorDetectionEnabled(false, this)
            }
        }
    }

    private fun showBackgroundScanDisclaimer() {
        with(AlertDialog.Builder(this)) {
            setTitle(getString(R.string.alert_detect_new_session_title))
            setMessage(getString(R.string.alert_detect_new_session_desc).trimIndent())
            setPositiveButton(getString(R.string.button_ok)) { dialog, _ ->
                viewModel.setBackgroundSensorDetectionEnabled(true, this@SettingsActivity)
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                binding.backgroundScanEnabled.isChecked = false
                dialog.dismiss()
            }
            setOnCancelListener { dialog ->
                binding.backgroundScanEnabled.isChecked = false
                dialog.dismiss()
            }

            show()
        }
    }

    private fun setupThemeSwitch(){
        binding.themeSwitch.isChecked = viewModel.theme == AppCompatDelegate.MODE_NIGHT_YES
        binding.themeSwitch.setThumbDrawableRes(getThemeImage())

        binding.themeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Theme: change with $isChecked")
            changeTheme()
        }
    }

    fun changeTheme(){
        Timber.d("Change theme")
        viewModel.changeTheme()
        binding.themeSwitch.setThumbDrawableRes(getThemeImage())
    }

    private fun getThemeImage(): Int{
        return if(viewModel.theme == AppCompatDelegate.MODE_NIGHT_YES){
            R.drawable.ic_moon
        }else{
            R.drawable.ic_sun3
        }
    }

    //TODO: Remove this, only for testing
    private fun setupDelayLayout(){
        val delay = SessionDelayManager.getDelay(this)
        updateDelayTextView(delay)
        binding.delayLayout.setOnClickListener {
            showDelayDialog()
        }
    }

    //TODO: Remove this, only for testing
    fun showDelayDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Intervallo misurazioni")

        val input = EditText(this)
        input.setHint("Intervallo in secondi")
        input.inputType = InputType.TYPE_CLASS_NUMBER
        val layout  = LinearLayout(this)
        layout.setPadding(48)
        val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        input.layoutParams = layoutParams
        layout.addView(input)

        builder.setView(layout)
        builder.setPositiveButton("Salva") { dialog, which ->
            // Here you get get input text from the Edittext
            var delay = input.text.toString().toIntOrNull()
            if (delay != null) {
                SessionDelayManager.setDelay(this, delay)
                updateDelayTextView(delay)

            } else {
                Toast.makeText(this, "Inserisci un valore numerico intero", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        builder.setNegativeButton("Annulla") { dialog, which -> dialog.cancel() }

        builder.show()
    }
    //TODO: Remove this, only for testing
    fun updateDelayTextView(value: Int){
        binding.delayInSecondsTextView.text = "$value"
    }
}