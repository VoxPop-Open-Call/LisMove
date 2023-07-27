package it.lismove.app.android.deviceConfiguration

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentDeviceConfigurationBinding
import it.lismove.app.android.deviceConfiguration.WheelUtils.dimen
import it.lismove.app.android.deviceConfiguration.data.LismovePopupPage
import it.lismove.app.room.entity.SensorEntity


class DeviceConfigurationFragment(val position: Int,val callback: DeviceConfigCallback) : Fragment() {
    private val pages: List<LismovePopupPage> = listOf(
        LismovePopupPage(
            title = "Aggancia il sensore",
            description = "Attiva il bluetooth del telefono e monta sul mozzo della ruota anteriore il sensore Lis Move. Accertati di aver rimosso la pellicola protettiva dal vano batteria prima di montare il sensore",
            image = R.drawable.bike_config_step1,
            buttonText = null
        ),

        LismovePopupPage(
            title = "Seleziona la dimensione della ruota e il tipo della bici",
            description = "Se cambi bici, ricordati di riconfigurare il sensore per impostare il diametro della ruota della nuova bici",
            image = R.drawable.bike_config_step2,
            buttonText = null,
            showEditText = true
        ),
        LismovePopupPage(
            title = "Ricerca del sensore",
            description = "Fai girare la ruota della tua" +
                    " bici e attendi che il sensore di velocità sia collegato!",
            topImage = R.drawable.bike_config_step3,
            bottomImage = R.drawable.bike_config_step3_searching,
            showLoading = true
        ),
        LismovePopupPage(
            title = "Sensore connesso",
            description = "Congratulazioni. Ora puoi incominciare a utilizzare Lis Move!",
            topImage = R.drawable.bike_config_step3,
            bottomImage = R.drawable.bike_config_step3_connected,
            showLoading = false
        ),
        LismovePopupPage(
            title = "Sensore non connesso",
            description = "",
            topImage = R.drawable.bike_config_step3,
            bottomImage = R.drawable.bike_config_step3_error,
            showLoading = false,
            buttonText = "Contatta l'assistenza"
        )

    )
    private lateinit var binding: FragmentDeviceConfigurationBinding

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentDeviceConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setError(errorMessage: String){
        pages[4].description = errorMessage
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val page = pages[position]
        with(binding) {
            configTitle.text = page.title
            configDescription.text = page.description
            configEditText.setText(SensorEntity.SENSOR_ENTINTY_DEFAUTL_WHEEL_LABEL)
            configEditTextLayout.isVisible  = page.showEditText
            configBikeTypeEditTextLayout.isVisible = page.showEditText
            configBikeTypeEditText.setText(SensorEntity.SENSOR_ENTITY_NORMAL)
            configEditText.setOnClickListener {
                showWheelAlert()
            }
            configBikeTypeEditText.setOnClickListener {
                showBikeTypeAlert()
            }
            setImage(configImage, page.image)
            setImage(topImage, page.topImage)
            setImage(bottomImage, page.bottomImage)

            bottomButton.isVisible = false
        }
    }

    fun showWheelAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Dimensione in pollici")
        builder.setItems(dimen) { _ , which ->
            binding.configEditText.setText(dimen[which])
            callback.onWheelDimenConfirmed(dimen[which])
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showBikeTypeAlert(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tipo bici")
        val items = arrayOf(SensorEntity.SENSOR_ENTITY_NORMAL, SensorEntity.SENSOR_ENTITY_ELECTRIC)
        builder.setItems(items) { _, which->
            binding.configBikeTypeEditText.setText(items[which])
            callback.onBikeTypeConfirmed(items[which])
        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun setImage(view: ImageView, res: Int?){
        if(res != null){
            view.visibility = View.VISIBLE
            view.setImageDrawable(ResourcesCompat.getDrawable(resources, res, null))
        }else{
            view.visibility = View.GONE
        }

    }

}