package it.lismove.app.android.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.ui.CityPickerActivity
import it.lismove.app.android.authentication.useCases.data.DataIncompleteError
import it.lismove.app.android.databinding.FragmentBaseProfileDetailBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.ui.AddressPointAdjusterActivity
import it.lismove.app.android.initiative.ui.BasicPointAdjusterActivity
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.android.initiative.ui.parser.getHomeAddress
import me.ibrahimsn.lib.PhoneNumberKit
import timber.log.Timber
import java.util.*


abstract class BaseProfileDetailFragment: LisMoveFragment() {
    var _binding: FragmentBaseProfileDetailBinding? = null
    private val binding get() = _binding!!
    var datePickerDialog: DatePickerDialog? = null
    lateinit var phoneNumberKit: PhoneNumberKit

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == BasicPointAdjusterActivity.REQUEST_CODE) {
                Timber.d("POINTS UPDATED")
                val addressString =
                    result.data?.getStringExtra(BasicPointAdjusterActivity.INTENT_ADDRESS)

                val address = Gson().fromJson(addressString, WorkAddress::class.java)
                setUserAddress(address)
                binding.homeAddressTextField.setText(address.completeName)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBaseProfileDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        Timber.d("onCreateView")

        // Inflate the layout for this fragment
        _binding?.let {
            it.bornDateTextField.setOnClickListener { openDatePicker() }
            it.genderTextField.setOnClickListener { openGenderPicker() }
            it.bornDateTextField.setOnFocusChangeListener { v, hasFocus -> if(hasFocus) openDatePicker() }
            it.saveUserConfigButton.setOnClickListener {  confirmAccountConfig()}
            it.homeAddressTextField.setOnClickListener {
                resultLauncher.launch(AddressPointAdjusterActivity.getIntent(requireContext(), getUserAddress(), true))
            }
            it.bornDateLayout.errorIconDrawable = null
            activity?.let { activity ->
                phoneNumberKit = PhoneNumberKit(activity)
                phoneNumberKit.attachToInput(it.phoneNumberLayout, "it")
                (activity as? AppCompatActivity)?.let {
                    phoneNumberKit.setupCountryPicker(it)

                }

            }
        }
        return view
    }

    abstract fun updateAccount(
        name: String,
        surname: String,
        nickname: String,
        bornDate: String,
        gender: String,
        phoneNumber: String,
        iban: String
    )

    abstract fun getCityFromIntent(intent: Intent?): LisMoveCityEntity?

    abstract fun getUserAddress(): WorkAddress
    abstract fun setUserAddress(workAddress: WorkAddress)

    protected fun confirmAccountConfig(){
        with(binding){
            val name =   nameTextField.text.toString()
            val surname =   surnameTextField.text.toString()
            val nickname =   nickNameTextFIeld.text.toString()
            val birthDate =   bornDateTextField.text.toString()
            val gender =   genderTextField.text.toString()
            val phoneNumber = phoneNumberTextField.text.toString()
            val iban = ibanTextField.text.toString()
            updateAccount(name, surname, nickname, birthDate, gender, phoneNumber, iban)

        }
    }


    open fun onError(throwable: Throwable) {
        hideLoadingAlert()
        if (throwable is DataIncompleteError){
            with(binding){
                nameLayout.error = throwable.nameError
                surnameLayout.error = throwable.surnameError
                nickNameLayout.error = throwable.nicknameError
                bornDateLayout.error = throwable.dateError
                ibanLayout.error = throwable.ibanError
            }
        }else{
            showError(throwable.message ?: "Si è verificato un problema")
        }
    }

    fun clearError(){
        with(binding){
            nameLayout.error = null
            surnameLayout.error = null
            nickNameLayout.error = null
            ibanLayout.error = null
        }
    }

    private fun openDatePicker(){
        val cldr: Calendar = Calendar.getInstance()
        val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
        val month: Int = cldr.get(Calendar.MONTH)
        val year: Int = cldr.get(Calendar.YEAR)
        // date picker dialog
        datePickerDialog = DatePickerDialog(
                requireActivity(),
                { view, year, monthOfYear, dayOfMonth -> binding.bornDateTextField.setText("$dayOfMonth/${monthOfYear + 1}/$year") },
                year,
                month,
                day
        )
        datePickerDialog?.show()
        datePickerDialog?.setOnDismissListener {
            dismissKeyboard()
            binding.bornDateTextField.clearFocus()
        }
    }


    private fun openGenderPicker(){
        // setup the alert builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sesso")

        val gender = arrayOf("Maschio", "Femmina")
        builder.setItems(gender) { dialog, which ->
            binding.genderTextField.setText(gender[which])
        }
        val dialog = builder.create()
        dialog.show()
    }

    protected fun bindUserProfile(user: LisMoveUser) {
        with(binding) {
            user.firstName?.let { nameTextField.setText(it) }
            user.lastName?.let { surnameTextField.setText(it) }
            user.username?.let { nickNameTextFIeld.setText(it) }
            user.birthDate?.let { bornDateTextField.setText(DateTimeUtils.getReadableShortDate(it)) }
            user.gender?.let { genderTextField.setText(it) }
            user.phoneNumber?.let { phoneNumberTextField.setText(it) }
            homeAddressTextField.setText(user.getHomeAddress().completeName)
            user.iban?.let { ibanTextField.setText(it) }
        }
    }

    private fun openCityPickerActivity(){
        val intent = Intent(requireContext(), CityPickerActivity::class.java)
        startActivityForResult(intent, CityPickerActivity.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult $requestCode")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}