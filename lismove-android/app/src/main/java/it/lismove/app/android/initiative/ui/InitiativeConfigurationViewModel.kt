package it.lismove.app.android.initiative.ui

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.android.initiative.parser.asSeatEntity
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.initiative.ui.InitiativeConfigurationActivity.Companion.INTENT_ENROLLMENT
import it.lismove.app.android.initiative.ui.InitiativeConfigurationActivity.Companion.INTENT_FIRST_CONFIGURATION
import it.lismove.app.android.initiative.ui.InitiativeConfigurationActivity.Companion.INTENT_IS_FROM_REGISTRATION
import it.lismove.app.android.initiative.ui.data.InitiativeConfiguration
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.room.entity.*
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.lang.Exception

class InitiativeConfigurationViewModel(
    private val organizationRepository: OrganizationRepository,
    val userRepository: UserRepository,
    val tempPrefsRepository: TempPrefsRepository,
    var user: LisMoveUser,
) : ViewModel(), KoinComponent {

    var isFirstConfig = true
    var isFromRegistration = false
    var hasSeats = false
    var requireIban = false
    private var state: MutableStateFlow<Lce<InitiativeConfiguration>> =
        MutableStateFlow(LceLoading())
    var stateObservable = state.asLiveData()

    lateinit var initiative: EnrollmentWithOrganization
    var isInitiativeActive: Boolean = false
    lateinit var workAddresses: ArrayList<WorkAddress>
    lateinit var customField: List<UserCustomField>
    var  areCustomFieldExclusive = false

    var isPAOrganization: Boolean = false
        get() = initiative.organization.type == OrganizationEntity.TYPE_PA

    var updatableUser = user.copy()


    fun initState(intent: Intent) {
        viewModelScope.launch {
            state.emit(LceLoading())
            try {
                isFirstConfig = intent.getBooleanExtra(INTENT_FIRST_CONFIGURATION, false)
                isFromRegistration = intent.getBooleanExtra(INTENT_IS_FROM_REGISTRATION, false)
                val enrollmentString = intent.getStringExtra(INTENT_ENROLLMENT)
                val enrollment = Gson().fromJson(enrollmentString, EnrollmentEntity::class.java)

                val organization = organizationRepository.getOrganization(enrollment.organization)
                initiative = EnrollmentWithOrganization(enrollment, organization)
                val settings = organizationRepository.getSettings(organization.id)
                areCustomFieldExclusive = settings.exclusiveCustomField
                requireIban = settings.ibanRequirement

                customField =
                    userRepository.getUserCustomField(enrollment.id.toString(), organization.id)

                isInitiativeActive = enrollment.endDate > DateTimeUtils.getCurrentTimestamp()

                val initiativeWorkAddress =
                    user.workAddresses?.filter { it.organization == (initiative.organization.id) }
                        ?: listOf()
                workAddresses = ArrayList(initiativeWorkAddress.map {
                    WorkAddress(it).apply {
                        editable = isPAOrganization
                        deletable = isInitiativeActive
                    }
                })
                ensureAtLeastOneWorkAddressIfOrganizationPa()
                fetchSeatsIfPrivateOrganization(initiative.organization.id)
                state.emit(
                    LceSuccess(
                        InitiativeConfiguration(
                            updatableUser,
                            initiative,
                            customField,
                            areCustomFieldExclusive,
                            isInitiativeActive
                        )
                    )
                )
            } catch (e: Exception) {
                state.emit(LceError(e))
            }

        }
    }

    private suspend fun fetchSeatsIfPrivateOrganization(id: Long) {
        hasSeats = organizationRepository.getSeats(id).isNotEmpty()
    }

    // ======== COMMON METHODS =========

    fun getInitiativeConfiguration(): InitiativeConfiguration {
        return InitiativeConfiguration(updatableUser, initiative, customField,areCustomFieldExclusive, isInitiativeActive)
    }

    fun checkAllDataComplete() {
        if(updatableUser.isHomeAddressComplete().not()){
            throw Exception("Inserisci un indirizzo di casa completo")
        } else if(updatableUser.phoneNumber?.length ?: 0 < 4){
            throw Exception("Inserisci un numero di telefono valido")
        }else if(updatableUser.iban?.isValidIban() != true && requireIban){
            throw Exception("Inserisci un indirizzo iban valido")
        }
    }

    private suspend fun updateUser() {
        val res = userRepository.updateUserProfile(updatableUser)

        tempPrefsRepository.saveTempUser(res)

        user = res
        updatableUser = user.copy()
        val initiativeWorkAddress =
            updatableUser.workAddresses?.filter { it.organization == (initiative.organization.id) }
                ?: listOf()
        workAddresses = ArrayList(initiativeWorkAddress.map {
            WorkAddress(it).apply {
                editable = isPAOrganization
                deletable = isInitiativeActive
            }
        })

    }

    fun saveAll(): LiveData<Lce<Boolean>> = flow<Lce<Boolean>> {
        emit(LceLoading())
        checkAllDataComplete()
        consumeCodeIfFirstTime()
        if (initiative.organization.type == OrganizationEntity.TYPE_PA) {
            updatePAWorkAddress()
        } else {
            updatePrivateWorkAddress()
        }
        updateUser()
        updateCustomField()
        emit(LceSuccess(true))

    }.catch {
        emit(LceError(it))
    }.asLiveData()

    private suspend fun consumeCodeIfFirstTime() {
        if (isFirstConfig && initiative.enrollment.activationDate == null) {
            userRepository.consumeCode(user.uid, initiative.enrollment.code)
        }
    }


    fun updateCustomFieldValue(customFieldId: Long, value: Boolean) {
        customField.firstOrNull { it.customFieldId == customFieldId }?.value = value
    }

    private suspend fun updateCustomField() {
        customField.forEach {
            organizationRepository.setUserCustomField(it)
        }
    }


    /*
            WORK ADDRESS MANAGEMENT
     */

    fun setWorkAddress(id: Long, workAddress: WorkAddress) {
        getWorkAddress(id).apply {
            address = workAddress.address
            name = workAddress.name
            number = workAddress.number
            city = workAddress.city
            cityExtended = workAddress.cityExtended
            lat = workAddress.lat
            lng = workAddress.lng
        }
    }

    fun setHomeAddress(view: WorkAddress) {
        updatableUser.apply {
            homeAddress = view.address
            homeNumber = view.number
            homeCity = view.city
            homeCityExtended = view.cityExtended
            homeLatitude = view.lat
            homeLongitude = view.lng
        }
    }


    private fun getWorkAddress(id: Long): WorkAddress {
        return workAddresses.first { it.id == id }
    }


    // ======== PA METHODS =========

    // Used when organization type is PA
    private fun ensureAtLeastOneWorkAddressIfOrganizationPa() {
        if (isPAOrganization) {
            if (workAddresses.isEmpty()) {
                addEmptyWorkAddress()
            }
        }

    }

    fun setWorkAddressName(name: String?) {
        getPAWorkAddress().apply {
            this.name = name
        }
    }

    // Used when organization type is PA
    private suspend fun updatePAWorkAddress() {
        val otherInitiativeAddress = arrayListOf<SeatEntity>()
        user.workAddresses?.filter { it.organization != initiative.organization.id }?.let {
            otherInitiativeAddress.addAll(it)
        }

        val previousWorkAddress =
            user.workAddresses?.firstOrNull { it.organization == initiative.organization.id }
        val workAddress = workAddresses.first().asSeatEntity(initiative.organization.id)

        if (previousWorkAddress == null || !previousWorkAddress.isEqual(workAddress)) {
            val seat = userRepository.createSeat(workAddress, user)
            otherInitiativeAddress.add(seat)

            updatableUser.workAddresses = otherInitiativeAddress

            //Sposta questo dopo chiamata api
            val initiativeWorkAddress =
                updatableUser.workAddresses?.filter { it.organization == (initiative.organization.id) }
                    ?: listOf()
            workAddresses = ArrayList(initiativeWorkAddress.map {
                WorkAddress(it).apply {
                    editable = isPAOrganization
                    deletable = isInitiativeActive
                }
            })
        } else {
            Timber.d("Indirizzo invariato")
        }
    }

    //Used in PA
    private fun addEmptyWorkAddress(): WorkAddress {
        val workAddress = WorkAddress()
        workAddresses.add(workAddress)
        return workAddress
    }

    fun getPAWorkAddress(): WorkAddress {
        ensureAtLeastOneWorkAddressIfOrganizationPa()
        return workAddresses.first()
    }

    // ======== COMPANY METHODS =========

    private fun updatePrivateWorkAddress() {
        Timber.d("updatePrivateWorkAddress")

        val otherInitiativeAddress = arrayListOf<SeatEntity>()
        user.workAddresses?.filter { it.organization != initiative.organization.id }?.let {
            otherInitiativeAddress.addAll(it)
        }

        val currentOrganizationWA =
            workAddresses.map { it.asSeatEntity(initiative.organization.id) }
        otherInitiativeAddress.addAll(currentOrganizationWA)
        updatableUser.workAddresses = otherInitiativeAddress

        //Sposta questo dopo chiamata api
        val initiativeWorkAddress =
            updatableUser.workAddresses?.filter { it.organization == (initiative.organization.id) }
                ?: listOf()
        workAddresses = ArrayList(initiativeWorkAddress.map {
            WorkAddress(it).apply {
                editable = isPAOrganization
                deletable = isInitiativeActive
            }
        })

    }

    fun addCompanySeat(seatEntity: SeatEntity): WorkAddress? {
        val existingSeat = workAddresses.firstOrNull { it.id == seatEntity.id!!.toLong() }
        if (existingSeat != null) {
            return null
        } else {
            var newWorkAddress = WorkAddress(seatEntity).apply {
                editable = isPAOrganization
                deletable = isInitiativeActive
            }
            workAddresses.add(newWorkAddress)
            return newWorkAddress
        }
    }


    fun removeWorkAddress(workAddress: WorkAddress) {
        workAddresses.remove(workAddress)
    }


}


