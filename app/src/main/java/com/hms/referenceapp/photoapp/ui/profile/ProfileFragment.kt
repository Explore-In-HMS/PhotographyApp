package com.hms.referenceapp.photoapp.ui.profile

import android.util.Log
import android.widget.Toast
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

    override fun setupUi() {
        viewModel.profileUiState.value.let {
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
    }
}