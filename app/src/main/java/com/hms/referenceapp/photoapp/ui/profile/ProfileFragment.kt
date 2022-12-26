package com.hms.referenceapp.photoapp.ui.profile

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.databinding.FragmentProfileBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.load
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    override val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var authService: HuaweiIdAuthService
    private var userId = ""
    private var userName = ""

    override fun setupUi() {
        viewModel.profileUiState.value.let {
            userId = it.userProfile.id.toString()
            userName = it.userProfile.name.toString()
            binding.textViewUserName.text = it.userProfile.name
            if (it.userProfile.profileImage != null) {
                binding.imageViewUser.load(it.userProfile.profileImage)
            } else {
                Log.e("PROFILE FRAGMENT", "Image is null!")
            }
        }


        binding.buttonSignOut.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.btnAddFriends.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToAddFriendsFragment(
                userId,
                userName
            )
            findNavController().navigate(action)
        }
    }
}