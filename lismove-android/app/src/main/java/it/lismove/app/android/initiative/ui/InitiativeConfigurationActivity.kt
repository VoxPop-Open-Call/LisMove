package it.lismove.app.android.initiative.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import com.google.gson.Gson
import it.lismove.app.android.ExpandableAddressView
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.SplashScreenActivity
import it.lismove.app.android.databinding.ActivityInitiativeConfigurationBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.*
import it.lismove.app.android.general.utils.indexesOf
import it.lismove.app.android.initiative.ui.adapter.ExpandableAddress
import it.lismove.app.android.initiative.ui.data.InitiativeConfiguration
import it.lismove.app.android.initiative.ui.data.PdfViewerActivity
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.android.initiative.ui.parser.getHomeAddress
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SeatEntity
import me.ibrahimsn.lib.PhoneNumberKit
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class InitiativeConfigurationActivity : LisMoveBaseActivity(), LceView<InitiativeConfiguration> {
    lateinit var binding: ActivityInitiativeConfigurationBinding
    val viewModel: InitiativeConfigurationViewModel by inject()
    private var workAddressView: ArrayList<ExpandableAddressView> = ArrayList()
    var customFieldCheckBox: List<CheckBox> = listOf()
    var customFieldRadioButton: List<RadioButton> = listOf()
    var customFieldRadioGroup: RadioGroup? = null
    lateinit var phoneNumberKit: PhoneNumberKit

    companion object {
        const val INTENT_ENROLLMENT = "INTENT_ENROLLMENT"
        const val INTENT_FIRST_CONFIGURATION = "INTENT_FIRST_CONFIGURATION"
        const val INTENT_IS_FROM_REGISTRATION = "INTENT_IS_FROM_REGISTRATION"

        fun getIntent(
            ctx: Context,
            enrollment: EnrollmentEntity,
            firstConfiguration: Boolean,
            isFromRegistration: Boolean = false
        ): Intent {
            return Intent(ctx, InitiativeConfigurationActivity::class.java).apply {
                putExtra(INTENT_ENROLLMENT, Gson().toJson(enrollment))
                putExtra(INTENT_FIRST_CONFIGURATION, firstConfiguration)
                putExtra(INTENT_IS_FROM_REGISTRATION, isFromRegistration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitiativeConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSaveButton()
        binding.successGroup.isVisible = false
        viewModel.initState(intent)
        showBackButtonIfNotFromRegistration()
        viewModel.stateObservable.observe(this, LceDispatcher(this))
    }

    private fun showBackButtonIfNotFromRegistration() {
        supportActionBar?.setDisplayHomeAsUpEnabled(viewModel.isFromRegistration.not())
    }


    override fun onSupportNavigateUp(): Boolean {
        askConfirmationIfFirstTimeOrDismiss()
        return true
    }

    override fun onBackPressed() {
        askConfirmationIfFirstTimeOrDismiss()
    }

    private fun askConfirmationIfFirstTimeOrDismiss() {
        if (viewModel.isFirstConfig) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Vuoi uscire dall'aggiunta iniziativa?")
            builder.setMessage("Tornando indietro non riscatterai il codice iniziativa")
            builder.setPositiveButton(
                "Sì"
            ) { dialog, id ->
                goToPreviousScreenAfterDismiss()
            }
            builder.setNegativeButton("Annulla") { dialog, id ->
            }
            builder.show()
        } else {
            goToPreviousScreenAfterDismiss()
        }
    }


    override fun onLoading() {
        binding.loadingBar.isIndeterminate = true
        binding.loadingBar.visibility = View.VISIBLE
    }

    override fun onSuccess(data: InitiativeConfiguration) {
        binding.loadingBar.visibility = View.GONE
        with(binding) {
            binding.successGroup.isVisible = true

            ibanLayout.isVisible = viewModel.requireIban
            ibanTextField.setText(data.user.iban)

            setInitiativeRules(
                data.initiative.organization.termsConditions,
                data.initiative.organization.getSanitizedRegulation()
            )

            homeAddressTextField.setText(data.user.getHomeAddress().completeName)
            homeAddressTextField.setOnClickListener {
                var address = data.user.getHomeAddress()
                openAddressPicker(true, address)
            }

            when (data.initiative.organization.type) {
                OrganizationEntity.TYPE_COMPANY -> setupPrivateOrganizationLayout(
                    data.initiative.organization.id,
                    data.active
                )
                OrganizationEntity.TYPE_PA -> setupPublicOrganizationLayout(data.active)
            }
            setupCommonReadOnlyView(data.active)
            setupPhoneNumber(data.active, data.user)
            populateCustomField(data)
        }
    }

    private fun setupPhoneNumber(active: Boolean, user: LisMoveUser) {
        phoneNumberKit = PhoneNumberKit(this)
        phoneNumberKit.attachToInput(binding.phoneNumberLayout, "it")
        phoneNumberKit.setupCountryPicker(this)
        binding.phoneNumberTextField.isEnabled = active
        user.phoneNumber?.let { binding.phoneNumberTextField.setText(it) }

        if (active) {
            phoneNumberKit.setupCountryPicker(this)
        } else {
            binding.phoneNumberLayout.setStartIconOnClickListener { }
        }
    }

    private fun setupCommonReadOnlyView(isActive: Boolean) {
        with(binding) {
            homeAddressTextField.isEnabled = isActive
            saveInitiativeButton.isVisible = isActive
        }
    }

    private fun populateCustomField(data: InitiativeConfiguration) {
        customFieldRadioGroup?.let { (it.parent as ViewGroup).removeView(it) }
        binding.customFieldTitle.isVisible = data.customField.isNotEmpty()
        if (data.customFieldExclusive) {
            setupExclusiveCustomField(data)

        } else {
            setupNonExclusiveCustomField(data)
        }

    }

    private fun setupNonExclusiveCustomField(data: InitiativeConfiguration) {
        customFieldCheckBox.forEach { (it.parent as ViewGroup).removeView(it) }

        customFieldCheckBox = data.customField.map {
            CheckBox(this).apply {
                setText("${it.name}\n${it.description}")
                isChecked = it.value
                gravity = Gravity.TOP
                tag = it.customFieldId
                isEnabled = data.active
            }
        }

        customFieldCheckBox.forEach { checkBox ->
            binding.customFieldsLayout.addView(checkBox)
        }
    }

    private fun setupExclusiveCustomField(data: InitiativeConfiguration) {
        customFieldRadioButton = data.customField.map {
            RadioButton(this).apply {
                setText("${it.name}\n${it.description}")
                isChecked = it.value
                gravity = Gravity.TOP
                tag = it.customFieldId
                isEnabled = data.active
            }
        }
        customFieldRadioGroup = RadioGroup(this)
        customFieldRadioButton.forEach { customFieldRadioGroup?.addView(it) }
        binding.customFieldsLayout.addView(customFieldRadioGroup)
    }


    override fun onError(throwable: Throwable) {
        binding.loadingBar.visibility = View.GONE
        showError(throwable.localizedMessage ?: "", binding.root)
    }

    private fun setupPublicOrganizationLayout(isActive: Boolean) {
        with(binding) {
            addSeatLayout.isVisible = false
            paAddressRelativeLayout.isVisible = true
            paNameLayout.isVisible = true

            val workAddress = viewModel.getPAWorkAddress()

            paAddressTextField.setOnClickListener { openAddressPicker(false, workAddress) }
            paNameTextField.setText(workAddress.name)
            paAddressTextField.setText(workAddress.completeName)

            paNameTextField.isEnabled = isActive
            paAddressTextField.isEnabled = isActive
        }
    }

    private fun setupPrivateOrganizationLayout(organizationId: Long, isActive: Boolean) {
        with(binding) {
            addSeatLayout.isVisible = isActive && viewModel.hasSeats
            paAddressRelativeLayout.isVisible = false
            paNameLayout.isVisible = false

            addAddressButton.setOnClickListener { openCompanySeatPicker(organizationId) }
            viewModel.workAddresses.forEach { workAddress ->
                val existingAddress =
                    workAddressView.firstOrNull { it.expandableAddress.address.id == workAddress.id }
                if (existingAddress == null) {
                    createAddressView(
                        showName = true,
                        isOpen = false,
                        address = workAddress,
                        showMaps = false,
                    )
                }
            }
            setupNoSeatAddedTextField(isActive)
        }
    }

    private fun setupNoSeatAddedTextField(isActive: Boolean) {
        with(binding) {
            if (isActive.not()) {
                noSeatAdded.text = "Nessuna sede inserita"
                noSeatAdded.isVisible = viewModel.workAddresses.isEmpty()
            } else {
                noSeatAdded.text = "Nessuna sede selezionabile"
                noSeatAdded.isVisible = viewModel.hasSeats.not()
            }
        }
    }

    private fun setupSaveButton() {
        with(binding) {
            saveInitiativeButton.setOnClickListener {
                saveDataBeforeSwitchingActivity()
                try {
                    viewModel.checkAllDataComplete()
                    saveAllData()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@InitiativeConfigurationActivity,
                        e.localizedMessage ?: "Inserisci tutti i dati", Toast.LENGTH_SHORT
                    ).show()
                    workAddressView.forEach {
                        it.checkError()
                    }
                }
            }
        }
    }

    private fun saveDataBeforeSwitchingActivity() {
        currentFocus?.clearFocus()
        updatePAWorkAddressName()
        viewModel.updatableUser.phoneNumber = binding.phoneNumberTextField.text?.toString()
        if(viewModel.requireIban){
            viewModel.updatableUser.iban = binding.ibanTextField.text?.toString()
        }
        if (viewModel.areCustomFieldExclusive) {
            customFieldRadioButton.forEach {
                viewModel.updateCustomFieldValue(
                    it.tag as Long,
                    it.isChecked
                )
            }
        } else {
            customFieldCheckBox.forEach {
                viewModel.updateCustomFieldValue(
                    it.tag as Long,
                    it.isChecked
                )
            }
        }
    }

    private fun updatePAWorkAddressName() {
        if (viewModel.isPAOrganization) {
            viewModel.setWorkAddressName(binding.paNameTextField.text?.toString())
        }
    }

    private fun saveAllData() {
        viewModel.saveAll().observe(this) {
            Timber.d("$it")
            when (it) {
                is LceLoading -> showLoadingAlert()
                is LceSuccess -> {
                    Timber.d("On Success, finish activity")
                    hideLoadingAlert()
                    goToNextScreenAfterDataSaved()
                }
                is LceError -> {
                    hideLoadingAlert()
                    onError(it.error)
                }
            }
        }
    }

    fun goToNextScreenAfterDataSaved() {
        if (viewModel.isFromRegistration) {
            goToSplashScreen()
        } else {
            finish()
        }

    }

    fun goToPreviousScreenAfterDismiss() {
        if (viewModel.isFromRegistration) {
            goToSplashScreen()
        } else {
            finish()
        }
    }

    private fun goToSplashScreen() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        finish()
    }

    private fun openAddressPicker(homeAddress: Boolean, workAddress: WorkAddress) {
        saveDataBeforeSwitchingActivity()
        resultLauncher.launch(
            AddressPointAdjusterActivity.getIntent(this, workAddress, homeAddress)
        )
    }

    private fun createAddressView(
        showName: Boolean,
        isOpen: Boolean,
        address: WorkAddress,
        showMaps: Boolean
    ): ExpandableAddressView {
        val addressView = ExpandableAddressView(this@InitiativeConfigurationActivity)
        addressView.setupAddress(
            ExpandableAddress(
                isInitiallyOpen = isOpen,
                showName = showName,
                address = address,
                showMapsButton = showMaps
            )
        )
        setAddressViewListeners(addressView, address)
        addAddressViewToUI(addressView)
        return addressView
    }

    private fun setAddressViewListeners(addressView: ExpandableAddressView, address: WorkAddress) {
        addressView.onDeleteSelected = { removePrivateWorkAddress(address, addressView) }
    }

    private fun addAddressViewToUI(addressView: ExpandableAddressView) {
        workAddressView.add(addressView)
        binding.companyAddressListLayout.addView(addressView)
    }

    private fun removeAddressViewToUI(addressView: ExpandableAddressView) {
        workAddressView.remove(addressView)
        binding.companyAddressListLayout.removeView(addressView)
    }

    private fun removePrivateWorkAddress(
        workAddress: WorkAddress,
        addressView: ExpandableAddressView
    ) {
        viewModel.removeWorkAddress(workAddress)
        removeAddressViewToUI(addressView)
    }

    private fun openCompanySeatPicker(organizationId: Long) {
        saveDataBeforeSwitchingActivity()
        resultLauncher.launch(
            CompanySeatPickerActivity.getIntent(this, organizationId)
        )
    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == BasicPointAdjusterActivity.REQUEST_CODE) {
                Timber.d("POINTS UPDATED")
                val addressString =
                    result.data?.getStringExtra(BasicPointAdjusterActivity.INTENT_ADDRESS)
                val isHomeAddress = result.data?.getBooleanExtra(
                    BasicPointAdjusterActivity.INTENT_IS_HOME_ADDRESS,
                    false
                ) ?: false
                val address = Gson().fromJson(addressString, WorkAddress::class.java)
                if (!isHomeAddress) {
                    viewModel.setWorkAddress(address.id, address)
                } else {
                    viewModel.setHomeAddress(address)
                }

            } else if (result.resultCode == CompanySeatPickerActivity.REQUEST_CODE) {
                val seatString = result.data?.getStringExtra(CompanySeatPickerActivity.EXTRA_CITY)
                val seat = Gson().fromJson(seatString, SeatEntity::class.java)
                viewModel.addCompanySeat(seat)?.let {
                    createAddressView(true, false, it, false)
                }
            }
        }


    private fun setInitiativeRules(termsUrl: String?, regulation: String?) {
        with(binding) {
            val termsString = "termini e condizioni"
            val regulationString = "regolamento dell'iniziativa"

            if (!termsUrl.isNullOrEmpty() && !regulation.isNullOrEmpty()) {
                initiativeRulesTextView.isVisible = true
                val text =
                    "Cliccando su salva accetti i termini e condizioni e il regolamento dell'iniziativa"
                val spannable = SpannableStringBuilder(text)
                addSpannToSpannable(spannable, text, termsString) {
                    startActivity(
                        PdfViewerActivity.getIntent(
                            this@InitiativeConfigurationActivity,
                            termsUrl,
                            termsString
                        )
                    )
                }
                addSpannToSpannable(spannable, text, regulationString) {

                    showRegulationAlert(regulation)
                }
                initiativeRulesTextView.movementMethod = LinkMovementMethod.getInstance()
                initiativeRulesTextView.setText(spannable, TextView.BufferType.SPANNABLE)
            } else if (!termsUrl.isNullOrEmpty()) {
                val text = "Cliccando su salva accetti i termini e condizioni"
                val spannable = SpannableStringBuilder(text)
                addSpannToSpannable(spannable, text, termsString) {
                    startActivity(
                        PdfViewerActivity.getIntent(
                            this@InitiativeConfigurationActivity,
                            termsUrl,
                            termsString
                        )
                    )

                }
                initiativeRulesTextView.movementMethod = LinkMovementMethod.getInstance()
                initiativeRulesTextView.setText(spannable, TextView.BufferType.SPANNABLE)
            } else if (!regulation.isNullOrEmpty()) {
                val text = "Cliccando su salva accetti il regolamento dell'iniziativa"
                val spannable = SpannableStringBuilder(text)
                addSpannToSpannable(spannable, text, regulationString) {
                    showRegulationAlert(regulation)
                }
                initiativeRulesTextView.movementMethod = LinkMovementMethod.getInstance()
                initiativeRulesTextView.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                initiativeRulesTextView.isVisible = false
            }

        }
    }


    private fun addSpannToSpannable(
        spannable: SpannableStringBuilder,
        text: String,
        spannableText: String,
        onClick: () -> Unit
    ) {
        val termsRange = text.indexesOf(spannableText)
        val termsSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                onClick()
            }
        }
        spannable.setSpan(
            termsSpan,
            termsRange.first(),
            termsRange.first() + spannableText.asSequence().count(),
            0
        )
    }

    private fun showRegulationAlert(regulation: String) {
        val container = FrameLayout(this)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(resources.getDimensionPixelSize(R.dimen.dialog_margin))
        val alertTextView = TextView(this)
        val spannableRule = SpannableString(regulation)
        Linkify.addLinks(spannableRule, Linkify.ALL)

        alertTextView.text = spannableRule
        alertTextView.movementMethod = LinkMovementMethod.getInstance()
        alertTextView.layoutParams = params
        container.addView(alertTextView)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Regolamento dell'iniziativa")
        builder.setView(container)
        builder.setPositiveButton("chiudi") { dialog, which -> }
        builder.show()
    }
}