/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.home


import android.content.ContentResolver
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexboxLayout
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.adapter.PhotoGalleryAdapter
import com.hms.referenceapp.photoapp.custom.AppPermission
import com.hms.referenceapp.photoapp.data.model.ClassificationModel
import com.hms.referenceapp.photoapp.data.model.PhotoModel
import com.hms.referenceapp.photoapp.databinding.FragmentHomeBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.*
import com.huawei.clustering.Cluster
import com.huawei.clustering.ClusterManager
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.MapStyleOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(FragmentHomeBinding::inflate),
    OnMapReadyCallback {

    override val viewModel: HomeViewModel by viewModels()

    private var recentlyPhotoModelList: List<PhotoModel>? = null
    private var classificationModelList: List<ClassificationModel>? = null
    private var parameterList: MutableList<String> = mutableListOf()

    private var previousListSize: Int? = null

    private var clusterManager: ClusterManager<PhotoModel>? = null

    private var mMapView: MapView? = null

    private val readExternalStoragePermissionResultLauncher = getPermissionResultLauncher(
        permission = AppPermission.ReadExternalStorage,
        onPermissionGranted = ::checkAccessMediaPermissionOrGetAllImagesByBuildVersion,
        onPermissionDenied = {
            showPermissionDialog(
                message = R.string.get_photo_permission_message,
                onClickListener = ::checkReadExternalStoragePermission
            )
        },
        onPermissionDeniedPermanently = {
            showPermissionDialog(
                message = R.string.get_photo_permission_message,
                onClickListener = ::goSettings
            )
        }
    )

    private val accessMediaLocationPermissionResultLauncher = getPermissionResultLauncher(
        permission = AppPermission.ReadExternalStorage,
        onPermissionGranted = ::getAllImagesFromSdCard,
        onPermissionDenied = {
            showPermissionDialog(
                message = R.string.access_media_location_permission_message,
                onClickListener = ::checkAccessMediaPermission
            )
        },
        onPermissionDeniedPermanently = {
            showPermissionDialog(
                message = R.string.access_media_location_permission_message,
                onClickListener = ::goSettings
            )
        }
    )

    private var hMap: HuaweiMap? = null

    @Inject
    lateinit var photoGalleryAdapter: PhotoGalleryAdapter

    override fun setupObservers() {
        collectLast(flow = viewModel.photoUiState, action = ::loadPhotos)
        collectLast(flow = viewModel.data, action = ::getAllData)

    }

    override fun setupUi() {
        with(binding) {
            recyclerviewGalleryImages.adapter = photoGalleryAdapter
        }
    }

    override fun setupListeners() {
        photoGalleryAdapter.setOnItemClickListener {
            val action =
                HomeFragmentDirections.actionHomeFragmentToOpenImageFragment(it)
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapView = binding.huaweiMap

        var mapViewBundle: Bundle? = null
        if (savedInstanceState == null) {
            mapViewBundle = savedInstanceState?.getBundle("MapViewBundleKey")
        }

        mMapView?.onCreate(mapViewBundle)
        mMapView?.getMapAsync(this)
    }

    private fun navigateToLoginPage() {
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onMapReady(hMap: HuaweiMap?) {
        this.hMap = hMap
        // Map Style
        val style: MapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle_night_hms)
        this.hMap?.setMapStyle(style)

        // Set Cluster
        loadPhotosWhichHasLocation(viewModel.getPhotoUiState().getPhotosWhichHasLocation())
    }

    private fun checkReadExternalStoragePermission() {
        if (hasPermission(permission = AppPermission.ReadExternalStorage)) {
            checkAccessMediaPermissionOrGetAllImagesByBuildVersion()
        } else {
            readExternalStoragePermissionResultLauncher.launch(AppPermission.ReadExternalStorage.name)
        }
    }

    private fun checkAccessMediaPermissionOrGetAllImagesByBuildVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAccessMediaPermission()
        } else {
            getAllImagesFromSdCard()
        }
    }

    private fun checkAccessMediaPermission() {
        if (hasPermission(permission = AppPermission.AccessMediaLocation)) {
            getAllImagesFromSdCard()
        } else {
            accessMediaLocationPermissionResultLauncher.launch(AppPermission.AccessMediaLocation.name)
        }
    }

    private fun getAllImagesFromSdCard() {
        viewModel.getAllImagesFromSdCard(requireContext().contentResolver)
    }

    private fun loadPhotos(photoUiState: PhotoUiState) {
        loadAllPhoto(photoUiState)
        loadPhotosWhichHasLocation(photoUiState.getPhotosWhichHasLocation())
    }

    private fun loadPhotosWhichHasLocation(photosWhichHasLocation: List<PhotoModel>) {
        setClustersToMap(photosWhichHasLocation)
        animateMapStartingLocation()
    }

    private fun setClustersToMap(photosWhichHasLocation: List<PhotoModel>) {
        hMap?.let {

            if (photosWhichHasLocation.isNotEmpty()) {
                clusterManager = ClusterManager<PhotoModel>(requireContext(), it)

                it.setOnCameraIdleListener {
                    clusterManager?.cluster()
                }

                clusterManager?.setCallbacks(object : ClusterManager.Callbacks<PhotoModel> {
                    override fun onClusterClick(cluster: Cluster<PhotoModel>): Boolean {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToClusteredPhotosFragment(
                                cluster.items.toTypedArray()
                            )
                        findNavController().navigate(action)
                        return false
                    }

                    override fun onClusterItemClick(clusterItem: PhotoModel): Boolean {
                        return false
                    }

                })
                clusterManager?.addItems(photosWhichHasLocation)
            }
        }
    }

    private fun animateMapStartingLocation() {
        val startingLocation = viewModel.getMapStartingLocation()
        startingLocation?.let {
            hMap?.animateCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    private fun loadAllPhoto(photoUiState: PhotoUiState) {
        with(binding) {
            recyclerviewGalleryImages.layoutManager =
                GridLayoutManager(requireContext(), getSpanCountByOrientation())
            photoGalleryAdapter.submitList(photoUiState.getAllPhotoPaths())
            setPhotoCount(photoUiState.getAllPhotoCount())

            // Get Previous Photos Count From Shared Preferences
            previousListSize = viewModel.getAllPhotosCount(requireContext())
            // Get Last 20 Photos
            recentlyPhotoModelList = photoUiState.getRecentlyPhotos()

            if (recentlyPhotoModelList!!.isNotEmpty()) {
                if (photoUiState.getAllPhotoCount() != previousListSize) {
                    // Update Photos Count in Shared Preferences
                    viewModel.setAllPhotosCount(
                        context = requireContext(),
                        allPhotosCount = photoUiState.getAllPhotoCount()
                    )
                    binding.tagProgressBar.visibility = VISIBLE
                    binding.tagFlexboxLayout.visibility = INVISIBLE
                    // Get Result From ML Kit
                    getThemeTagFromImage(
                        contentResolver = requireContext().contentResolver,
                        path = recentlyPhotoModelList!!,
                        requestFlag = true,
                        photoSize = photoUiState.getRecentlyPhotosCount()
                    )
                } else {
                    binding.tagProgressBar.visibility = VISIBLE
                    binding.tagFlexboxLayout.visibility = INVISIBLE
                    // Get Result From Room
                    getThemeTagFromImage(
                        contentResolver = requireContext().contentResolver,
                        path = recentlyPhotoModelList!!,
                        requestFlag = false,
                        photoSize = photoUiState.getRecentlyPhotosCount()
                    )
                }
            }
        }
    }

    private fun getThemeTagFromImage(
        contentResolver: ContentResolver,
        path: List<PhotoModel>,
        requestFlag: Boolean,
        photoSize: Int
    ) {
        viewModel.getThemeTagFromImage(
            contentResolver = contentResolver,
            path = path,
            requestFlag = requestFlag,
            photoSize = photoSize
        )
    }

    private fun getAllData(dataList: List<ClassificationModel>) {
            //Progress Bar State
            binding.tagProgressBar.visibility = INVISIBLE
            //Clear param list
            parameterList.clear()
            // Set data to Tag Click Listener
            classificationModelList = dataList

            // Add all tagNames to parameterList
            for (list in dataList) {
                for (parameters in list.resultParameters) {
                    parameterList.add(parameters.tagName)
                }
            }

            // Get List
            val filterAndSortParameterList =
                viewModel.filterAndSortParameterList(parameterList = parameterList)

            // Dynamically create TextView according to 10 item
            dynamicallyCreateTextView(filterAndSortParameterList)

    }

    private fun dynamicallyCreateTextView(filterAndSortParameterList: Map<String, Int>) {
        binding.tagFlexboxLayout.visibility = VISIBLE
        binding.tagFlexboxLayout.removeAllViews()
        for (list in filterAndSortParameterList) {
            val tagTextView = TextView(requireContext())
            val flexboxLayoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            flexboxLayoutParams.setMargins(5, 5, 8, 5)
            tagTextView.layoutParams = flexboxLayoutParams
            tagTextView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.tag_textview_style)
            tagTextView.text = list.key
            tagTextView.setTextColor(Color.WHITE)
            tagTextView.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToTagDetailFragment(
                        classificationModelList?.toTypedArray()
                    ).setTagName(list.key)
                findNavController().navigate(action)
            }
            binding.tagFlexboxLayout.addView(tagTextView)
        }

    }

    private fun setPhotoCount(count: Int) {
        binding.galleryNumber.text = getString(R.string.photo_count, count)
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isUserLogged().not()) {
            navigateToLoginPage()
            return
        }
        checkReadExternalStoragePermission()

        mMapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

}