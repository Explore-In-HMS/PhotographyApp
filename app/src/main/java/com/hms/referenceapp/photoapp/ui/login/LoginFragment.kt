/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.login

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.databinding.FragmentLoginBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.setVisibility
import com.hms.referenceapp.photoapp.util.ext.showToast
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginViewModel, FragmentLoginBinding>(
    FragmentLoginBinding::inflate
) {

    override val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var authService: HuaweiIdAuthService

    private var signInWithHuaweiID =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.userSignedIn(result.data)
            }
        }

    override fun setupListeners() {
        binding.loginbutton.setOnClickListener {
            signInWithHuaweiID.launch(authService.signInIntent)
            binding.loginbutton.isEnabled = false
        }
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.loginUiState, action = ::setUiState)
    }

    private fun setUiState(loginUiState: LoginUiState) {
        loginUiState.error.firstOrNull()?.let {
            showError(it)
            viewModel.errorShown()
        }
        loginUiState.isUSerSigned.firstOrNull()?.let {
            navigateHomePage()
            viewModel.navigatedHomePage()
        }
        binding.pbSignIn.setVisibility(loginUiState.loading)
    }

    private fun navigateHomePage() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun showError(errorMessage: String) {
        showToast(errorMessage)
    }

}
