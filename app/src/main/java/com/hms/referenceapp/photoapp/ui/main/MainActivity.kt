/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.main

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.databinding.ActivityMainBinding
import com.hms.referenceapp.photoapp.ui.base.BaseActivity
import com.hms.referenceapp.photoapp.util.ext.gone
import com.hms.referenceapp.photoapp.util.ext.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun getActivityViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun setup() {
        setupNavHostFragment()
    }

    private fun setupNavHostFragment() {
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment)
        navController = navHostFragment.navController

        setupBottomNavigationView(navHostFragment)
    }

    private fun setupBottomNavigationView(navHostFragment: NavHostFragment) {
        with(activityViewBinding) {
            homeBottomNavigationView.setupWithNavController(navHostFragment.navController)

            navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment -> {
                        homeBottomNavigationView.gone()
                    }
                    R.id.openImageFragment -> {
                        homeBottomNavigationView.gone()
                    }
                    R.id.editImageFragment -> {
                        homeBottomNavigationView.gone()
                    }
                    else -> {
                        homeBottomNavigationView.show()
                    }
                }
            }
        }
    }
}