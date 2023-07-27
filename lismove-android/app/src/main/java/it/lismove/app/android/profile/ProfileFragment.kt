package it.lismove.app.android.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import coil.load
import coil.transform.CircleCropTransformation
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import it.lismove.app.android.R
import it.lismove.app.android.authentication.repository.AuthRepositoryImpl
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.databinding.FragmentProfileBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import lv.chi.photopicker.PhotoPickerFragment
import org.koin.android.ext.android.inject
import timber.log.Timber

class ProfileFragment : LisMoveFragment(R.layout.fragment_profile),  PhotoPickerFragment.Callback, LceView<LisMoveUser> {
    private lateinit var binding: FragmentProfileBinding

    private val profileDetailFragment = ProfileWrapperFragment()

    private val viewModel: ProfileFragmentViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Profilo"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)
        showProfileDetail()
        bindProfileInfo(viewModel.user)
        showProfileDetail()
        viewModel.stateLiveData.observe(viewLifecycleOwner, LceDispatcher(this))

    }


    override fun onResume() {
        super.onResume()
        Timber.d("On resume, user is ${viewModel.user.username}")
    }

    private fun bindProfileInfo(user: LisMoveUser){
        with(binding){
            profileNickname.text = user.username
            profileEmail.text = user.email
            val authProfile = AuthRepositoryImpl().getCurrentAuthUser()?.photoUrl
            Timber.d("authProfileUrl $authProfile")
           if(user.avatarUrl.isNullOrEmpty()){
                profileImage.load(R.drawable.ic_notification_session){
                    transformations(CircleCropTransformation())
                }
            }else{
                val authProfile = AuthRepositoryImpl().getCurrentAuthUser()?.photoUrl
                Timber.d("authProfileUrl $authProfile")
                profileImage.load(user.avatarUrl){
                    transformations(CircleCropTransformation())
                }
             }



            profileImage.setOnClickListener { openImagePicker() }

        }
    }

    private fun showProfileDetail(){
        childFragmentManager.beginTransaction().apply {
            replace(R.id.profileContentFragment, profileDetailFragment)
            commit()
        }
    }


    fun openImagePicker(){
        PhotoPickerFragment.newInstance(
            multiple = false,
            allowCamera = true,
            maxSelection = 1,
            theme = R.style.ChiliPhotoPicker_Light_LisMove
        ).show(childFragmentManager, "photo_fragment")
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        if (photos.isNotEmpty()) {
            viewModel.updatePhoto(photos.first())
        }
    }

    override fun onLoading() {
        showLoadingAlert()
    }

    override fun onSuccess(data: LisMoveUser) {
        hideLoadingAlert()
        bindProfileInfo(data)
    }

    override fun onError(throwable: Throwable) {
        hideLoadingAlert()
        showError(throwable.localizedMessage)
    }
}