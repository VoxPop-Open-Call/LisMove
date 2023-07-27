package it.lismove.app.android.other

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import it.lismove.app.android.R
import it.lismove.app.android.car.ui.CarConfigurationActivity
import it.lismove.app.android.databinding.FragmentOtherBinding
import it.lismove.app.android.initiative.ui.MyInitiativeActivity
import it.lismove.app.android.profile.ProfileActivity
import it.lismove.app.android.settings.SensorDetailActivity
import it.lismove.app.android.settings.SettingsActivity
import org.koin.android.ext.android.inject

class OtherFragment : Fragment(R.layout.fragment_other) {

    private lateinit var binding: FragmentOtherBinding
    val viewModel: OtherViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Altro"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOtherBinding.bind(view)

        with(binding){
            versionNumber.text = viewModel.getVersionNumber()
            initiativeCodeMenuItem.setOnClickListener {
               openInitiativeActivity()
            }
            settingsMenuItem.setOnClickListener {
                openSettings()
            }
            profileMenuItem.setOnClickListener {
                openProfile()
            }
            sensorMenuItem.setOnClickListener {
                openSensorDetail()
            }
            helpAndFaqMenuItem.setOnClickListener {
                openHelpAndFaq()
            }
            infoAndConditionsMenuItem.setOnClickListener {
                openInfoAndConditions()
            }
            carMenuItem.setOnClickListener {
                startActivity(Intent(requireActivity(), CarConfigurationActivity::class.java))
            }

        }
    }

    private fun openHelpAndFaq(){
        context?.let {
            startActivity(Intent(it, HelpAndFaqActivity::class.java))
        }
    }

    private fun openProfile(){
        context?.let {
            startActivity(Intent(it, ProfileActivity::class.java ))
        }
    }

    private fun openSettings(){
        context?.let {
            startActivity(Intent(it, SettingsActivity::class.java))
        }
    }

    private fun openSensorDetail(){
        context?.let {
            startActivity(Intent(it, SensorDetailActivity::class.java))
        }
    }

    private fun openInitiativeActivity(){
        context?.let {
            startActivity(Intent(it, MyInitiativeActivity::class.java))
        }
    }
    private fun openInfoAndConditions(){
        context?.let {
            startActivity(Intent(it, InfoActivity::class.java))
        }
    }

}