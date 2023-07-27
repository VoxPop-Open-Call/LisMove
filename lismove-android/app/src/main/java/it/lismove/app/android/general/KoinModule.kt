package it.lismove.app.android.general

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import it.lismove.app.android.MainActivityViewModel
import it.lismove.app.android.authentication.repository.*
import it.lismove.app.android.authentication.ui.*
import it.lismove.app.android.authentication.useCases.AuthenticationUseCase
import it.lismove.app.android.authentication.useCases.EditProfileUseCase
import it.lismove.app.android.authentication.useCases.EmailSignInUseCase
import it.lismove.app.android.authentication.useCases.LogOutUseCase
import it.lismove.app.android.authentication.useCases.impl.*
import it.lismove.app.android.awards.AwardDetailViewModel
import it.lismove.app.android.awards.AwardViewModel
import it.lismove.app.android.awards.AwardWrapperViewModel
import it.lismove.app.android.awards.repository.AwardRepository
import it.lismove.app.android.awards.repository.AwardRepositoryImpl
import it.lismove.app.android.car.repository.CarRepository
import it.lismove.app.android.car.repository.CarRepositoryImpl
import it.lismove.app.android.car.ui.CarConfigurationViewModel
import it.lismove.app.android.car.ui.CarWizardViewModel
import it.lismove.app.android.chat.ChatManager
import it.lismove.app.android.chat.ChatManagerImpl
import it.lismove.app.android.chat.ChatManagerMockImpl
import it.lismove.app.android.dashboard.DashboardViewModel
import it.lismove.app.android.dashboard.repository.DashboardRepository
import it.lismove.app.android.dashboard.repository.DashboardRepositoryImpl
import it.lismove.app.android.dashboard.useCases.*
import it.lismove.app.android.deviceConfiguration.DeviceConfigurationViewModel
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.deviceConfiguration.repository.SensorRepositoryImpl
import it.lismove.app.android.deviceConfiguration.repository.SessionConfigRepository
import it.lismove.app.android.deviceConfiguration.repository.SessionConfigRepositoryImpl
import it.lismove.app.android.gaming.repository.AchievementRepository
import it.lismove.app.android.gaming.repository.impl.AchievementsRepositoryImpl
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.initiative.repository.OrganizationRepositoryImpl
import it.lismove.app.android.initiative.ui.*
import it.lismove.app.android.maps.MapViewModel
import it.lismove.app.android.other.OtherViewModel
import it.lismove.app.android.settings.SensorDetailViewModel
import it.lismove.app.android.settings.SettingsViewModel
import it.lismove.app.android.profile.ProfileFragmentViewModel
import it.lismove.app.android.profile.ProfileUserDetailViewModel
import it.lismove.app.android.gaming.repository.RankingRepository
import it.lismove.app.android.gaming.repository.impl.RankingRepositoryImpl
import it.lismove.app.android.gaming.ui.AchievementViewModel
import it.lismove.app.android.gaming.ui.ActiveAwardsViewModel
import it.lismove.app.android.gaming.ui.RankingViewModel
import it.lismove.app.android.logWall.LogWallViewModel
import it.lismove.app.android.logWall.repository.LogWallRepository
import it.lismove.app.android.logWall.repository.LogWallRepositoryImpl
import it.lismove.app.android.maps.AddFountainViewModel
import it.lismove.app.android.maps.repository.DrinkingFountainRepository
import it.lismove.app.android.maps.repository.DrinkingFountainRepositoryImpl
import it.lismove.app.android.maps.repository.MapsRepository
import it.lismove.app.android.maps.repository.MapsRepositoryImpl
import it.lismove.app.android.notification.ui.NotificationListViewModel
import it.lismove.app.android.other.HelpAndFaqViewModel
import it.lismove.app.android.prefs.AlertPreferencesRepository
import it.lismove.app.android.prefs.AlertPreferencesRepositoryImpl
import it.lismove.app.android.prefs.LatestVersionRepository
import it.lismove.app.android.prefs.LatestVersionRepositoryImpl
import it.lismove.app.android.profile.ProfileWrapperViewModel
import it.lismove.app.android.session.repository.*
import it.lismove.app.android.session.ui.SessionsHistoryViewModel
import it.lismove.app.android.session.ui.SessionDetailViewModel
import it.lismove.app.android.session.ui.SessionFeedbackViewModel
import it.lismove.app.android.session.ui.SessionViewModel
import it.lismove.app.android.session.ui.useCase.SessionDetailUseCase
import it.lismove.app.android.session.ui.useCase.SessionDetailUseCaseImpl
import it.lismove.app.android.session.useCases.SessionCachingUseCase
import it.lismove.app.android.session.useCases.SessionUploadUseCase
import it.lismove.app.android.session.useCases.impl.SessionCachingUseCaseImpl
import it.lismove.app.android.session.useCases.impl.SessionUploadUseCaseImpl
import it.lismove.app.android.theme.ThemeRepository
import it.lismove.app.android.theme.ThemeRepositoryImpl
import it.lismove.app.utils.TempPrefsRepository
import it.lismove.app.utils.TempPrefsRepositoryImpl
import net.nextome.lismove_sdk.LismoveSensorSdk
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


class KoinModule {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "lismove_datastore")

        fun getModule() = module {
            single<TempPrefsRepository> { TempPrefsRepositoryImpl(androidContext()) }
            single<DataStore<Preferences>> { androidApplication().dataStore }

            //Repository
            single<AuthRepository> { AuthRepositoryImpl() }
            single<PhoneRepository> { PhoneRepositoryImpl(androidContext()) }
            single<UserRepository> { UserRepositoryImpl(get(), get(), get(), get(), get(), get()) }
            single<SensorRepository> { SensorRepositoryImpl(get(), get(), get()) }
            single< SensorHistoryRepository> { SensorHistoryRepositoryImpl(get()) }
            single<SessionConfigRepository> { SessionConfigRepositoryImpl(get()) }
            single<CityRepository> { CityRepositoryImpl(androidContext()) }
            single<ApplicationSessionRepository> { ApplicationSessionRepositoryImpl(get(), get(), get(), get()) }
            single<ThemeRepository> { ThemeRepositoryImpl(androidContext()) }
            single<OrganizationRepository> { OrganizationRepositoryImpl(get(), get(), get(), get()) }
            single<RankingRepository> { RankingRepositoryImpl(get(), get()) }
            single<AchievementRepository> { AchievementsRepositoryImpl(get(), get()) }
            single<MapsRepository> { MapsRepositoryImpl() }
            single<DashboardRepository> {DashboardRepositoryImpl(get(), get(), get(), androidContext())}
            single<CarRepository>{CarRepositoryImpl(get())}
            single<DrinkingFountainRepository>{DrinkingFountainRepositoryImpl()}
            single<LogWallRepository> { LogWallRepositoryImpl(get()) }
            single<AwardRepository> {AwardRepositoryImpl(get(), get())}
            single<LatestVersionRepository> { LatestVersionRepositoryImpl() }

            //Use cases
            single<AuthenticationUseCase> { AuthenticationUseCaseImpl(get(), get(), get(), get(), get()) }
            single<EmailSignInUseCase> {EmailSignInUseCaseImpl(get(), get())}
            single<EditProfileUseCase> { EditProfileUseCaseImpl(get(), get()) }
            single<SessionUploadUseCase> { SessionUploadUseCaseImpl(get(), get()) }
            single<SessionCachingUseCase> {SessionCachingUseCaseImpl(get(), get())}
            single<TotalPointsUseCase> { TotalPointsUseCaseImpl(get(), get())}
            single<LogOutUseCase> {LogOutUseCaseImpl(androidContext(), get(), get(), get(), get(), get())}
            single<SessionDetailUseCase> { SessionDetailUseCaseImpl(get(), get(), get(), get(), get()) }
            // Session
            single { LismoveSensorSdk() }

            //Managers
            single<ChatManager> {ChatManagerImpl()}
            single<AlertPreferencesRepository> {  AlertPreferencesRepositoryImpl(androidContext())}
            //ViewModels
            viewModel { InitiativeConfigurationViewModel(get(), get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { AccountConfigurationViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { CityPickerViewModel(get()) }
            viewModel { EmailSignInViewModel(get()) }
            viewModel { ProfileUserDetailViewModel(get<TempPrefsRepository>().getTempUser(), get(), get()) }
            viewModel { EmailConfirmationViewModel(get(), get()) }
            viewModel { SignUpViewModel(get(), get()) }
            viewModel { SignInViewModel(get()) }
            viewModel { MapViewModel(get(), get(), get(), get(),get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { LicenceAgreementViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { SplashScreenViewModel(get(), get(), get()) }
            viewModel { SessionViewModel(androidContext(), get(), get(), get(), get(), get(), get(), get(), get(), get(),get<TempPrefsRepository>().getTempUser()) }
            viewModel { DeviceConfigurationViewModel(get(), get(), get(), get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { SessionDetailViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { ProfileFragmentViewModel( get<TempPrefsRepository>().getTempUser(), get()) }
            viewModel { ResetPasswordViewModel(get(), get()) }
            viewModel { MainActivityViewModel(get(), get(), get<TempPrefsRepository>().isConfigSent(),
                        get(), get(), get(), get(), get(), get(), get(), get<TempPrefsRepository>().getTempUser())  }
            viewModel { SensorDetailViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { SessionsHistoryViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { OtherViewModel(androidContext()) }
            viewModel { RegistrationCodeViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { SettingsViewModel(get(), get(), get(),) }
            viewModel { CompanySeatPickerViewModel(get()) }
            viewModel { MyInitiativeViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { RankingViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { AchievementViewModel(get(),get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { DashboardViewModel(get(),get(),get(), get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { AddressPointAdjusterViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { CarConfigurationViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { CarWizardViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { ProfileWrapperViewModel(get())}
            viewModel { HelpAndFaqViewModel(get(),get<TempPrefsRepository>().getTempUser()) }
            viewModel { LogWallViewModel(get()) }
            viewModel { AwardViewModel(get()) }
            viewModel { AwardWrapperViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { AwardDetailViewModel() }
            viewModel { NotificationListViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { AddFountainViewModel(get(), get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel { ActiveAwardsViewModel(get(), get<TempPrefsRepository>().getTempUser()) }
            viewModel {SessionFeedbackViewModel(get(), get(), get<TempPrefsRepository>().getTempUser())}
        }
    }
}