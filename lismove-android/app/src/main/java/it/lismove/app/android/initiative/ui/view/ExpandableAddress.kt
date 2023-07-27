package it.lismove.app.android

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import it.lismove.app.android.databinding.ItemExpandableTextFieldBinding
import it.lismove.app.android.initiative.ui.adapter.ExpandableAddress
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.room.entity.LisMoveCityEntity
import timber.log.Timber

class ExpandableAddressView  @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr){

    private var binding: ItemExpandableTextFieldBinding = ItemExpandableTextFieldBinding.inflate(LayoutInflater.from(context))
    var expandableAddress: ExpandableAddress = ExpandableAddress(showMapsButton = true)

    var onCitySelected: () ->Unit = {}
    var onSaveSelected: (ExpandableAddress) ->Unit = {}
    var onDeleteSelected: () ->Unit = {}
    var onMapsSelected: (ExpandableAddress) ->Unit = {}

    init { // inflate binding and add as view
        addView(binding.root)
        setupAddress(ExpandableAddress(showMapsButton = true))
    }

    override fun refreshDrawableState() {
        super.refreshDrawableState()
    }

    fun setupAddress(data: ExpandableAddress){
        expandableAddress = data
        with(binding){

            itemLocationLayout.editText?.setText(data.completeName) //resume
            innerAddressNameLayout.editText?.setText(data.address.name)
            innerAddressLayout.editText?.setText(data.address.address)
            innerAddressNumberLayout.editText?.setText(data.address.number)
            addressCityLayout.editText?.setText(data.address.cityExtended?.getFullName())
            innerAddressTextField.isEnabled = data.address.editable
            innerAddressNameTextField.isEnabled = data.address.editable
            innerAddressNumberTextField.isEnabled = data.address.editable
            innerAddressNumberLayout.errorIconDrawable = null
            addressCityLayout.isEnabled = data.address.editable
            innerAddressNameLayout.isVisible = data.showName
            setToggleBehaviour()
            setToggleIcon()

            if(data.address.editable){
                addressCityTextField.setOnClickListener { onCitySelected() }
                saveButton.setOnClickListener {
                    update()
                    val hasError = checkError()
                    if(!hasError){
                        onSaveSelected(expandableAddress)
                    }
                }
            }else if(data.address.deletable){
                saveButton.icon = AppCompatResources.getDrawable(this.rootView.context, R.drawable.ic_baseline_delete_24)
                saveButton.text = "RIMUOVI"
                saveButton.setOnClickListener {onDeleteSelected()}
            }else{
                saveButton.isVisible = false
            }

            mapsButton.isVisible = data.showMapsButton
            mapsButton.setOnClickListener {
                Timber.d("Maps Selected")
                update()
                val hasError = checkError()
                if(!hasError){
                    onMapsSelected(expandableAddress)

                }
            }
        }
    }

    fun checkError(): Boolean{
        binding.innerAddressLayout.error = expandableAddress.address.getAddressError()
        binding.innerAddressNumberLayout.error = expandableAddress.address.getNumberError()
        binding.addressCityLayout.error = expandableAddress.address.getCityError()
        binding.innerAddressNameLayout.error = expandableAddress.address.getNameError()
        binding.itemLocationLayout.error = expandableAddress.address.getFullAddressError()
        return !expandableAddress.address.isComplete()
    }

    fun updateAndToggle(data: ExpandableAddress){
        setupAddress(data)
        toggle()
    }

    fun updateAddressAndToggle(data: WorkAddress){
        setupAddress(expandableAddress.copy(address = data))
        if(binding.expandableLayout.isExpanded){
            toggle()
        }
    }


    fun getCurrentAddress(): ExpandableAddress{
        update()
        return expandableAddress
    }

    fun updateCity(cityEntity: LisMoveCityEntity?){
        if(cityEntity?.id != expandableAddress.address.city){
            expandableAddress.address.lat = null
            expandableAddress.address.lng = null
        }
        expandableAddress.address.cityExtended = cityEntity
        expandableAddress.address.city = cityEntity?.id
        binding.addressCityLayout.editText?.setText(cityEntity?.getFullName())
    }

    fun update(){
        with(binding){
            val newAddress = innerAddressTextField.text.toString()
            val newName = innerAddressNameTextField.text.toString()
            val newNumber = innerAddressNumberTextField.text.toString()
            if(newAddress != expandableAddress.address.address || newNumber != expandableAddress.address.number ) {
                expandableAddress.address.lat = null
                expandableAddress.address.lng = null
            }
            expandableAddress.address.number = newNumber
            expandableAddress.address.name = newName
            expandableAddress.address.address = newAddress
        }
    }

    private fun setToggleBehaviour(){
        with(binding){
            expandedActionImage.setOnClickListener { toggle() }
            itemLocationTextField.setOnClickListener { toggle() }
        }
    }

    fun toggle(){
        with(binding){
            expandableLayout.toggle()
            setToggleIcon()
        }

    }

    fun setToggleIcon(){
        with(binding){
            val color = ResourcesCompat.getColor(resources, R.color.gray_image_tint, null)

            if(expandableLayout.isExpanded){
                val drawable = ResourcesCompat.getDrawable(resources,setCloseIcon(), null)
                drawable?.setTint(color)
                expandedActionImage.setImageDrawable(drawable)
            }else{
                val drawable = ResourcesCompat.getDrawable(resources, getExpandIcon(), null)
                drawable?.setTint(color)
                expandedActionImage.setImageDrawable(drawable)
            }
        }

    }

    fun getExpandIcon(): Int{
       return if(expandableAddress.address.editable) R.drawable.ic_baseline_edit_24 else R.drawable.ic_baseline_expand_more_24
    }

    fun setCloseIcon(): Int{
        return if(expandableAddress.address.editable) R.drawable.ic_baseline_close_24 else R.drawable.ic_baseline_expand_less_24

    }


}