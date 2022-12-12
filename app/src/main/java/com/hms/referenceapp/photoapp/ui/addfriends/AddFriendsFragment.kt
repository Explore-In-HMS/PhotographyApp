package com.hms.referenceapp.photoapp.ui.addfriends

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.hms.referenceapp.photoapp.adapter.ListUserAdapter
import com.hms.referenceapp.photoapp.adapter.PendingRequestAdapter
import com.hms.referenceapp.photoapp.databinding.FragmentAddFriendsBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddFriendsFragment :
    BaseFragment<AddFriendsViewModel, FragmentAddFriendsBinding>(FragmentAddFriendsBinding::inflate) {

    override val viewModel: AddFriendsViewModel by viewModels()

    private val args: AddFriendsFragmentArgs by navArgs()

    private var userId = ""
    private var userName = ""

    @Inject
    lateinit var listUserAdapter: ListUserAdapter

    @Inject
    lateinit var pendingRequestAdapter: PendingRequestAdapter

    override fun setupUi() {
        viewModel.addFriendsUiState.value.let {

        }

        userId = args.userId.toString()
        viewModel.userId = userId

        userName = args.userName.toString()
        viewModel.userName = userName

        setAdapters()
        viewModel.getUsers()
        args.userId?.let { viewModel.getPendingRequests(currentUserId = it) }

        binding.edtSearchUser.addTextChangedListener {
            if (binding.edtSearchUser.text != null || binding.edtSearchUser.text.isNotEmpty()) {
                listUserAdapter.setUserList(viewModel.getFilteredList(binding.edtSearchUser.text.toString()))
            }
        }

        binding.btnAddFriend.setOnClickListener {
            val userList = viewModel.getFilteredList("")

            userList.forEach {
                if (it.isChecked) {
                    val secondUserId = it.user.id.toString()
                    val secondUserName = it.user.name
                    viewModel.sendFriendRequest(userId, secondUserId, userName, secondUserName)
                    showToast("Friend request sent!!")
                }
                it.isChecked = false
            }


            listUserAdapter.setUserList(userList)
        }

    }

    private fun setAdapters() {
        binding.recyclerviewUsers.adapter = listUserAdapter
        binding.recyclerviewPendingRequest.adapter = pendingRequestAdapter
        pendingRequestAdapter.setRequestList(viewModel.addFriendsUiState.value.pendingRequestList)
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.addFriendsUiState, action = ::setUiState)
    }

    override fun setupListeners() {
        pendingRequestAdapter.onRequestUpdateListener {
            viewModel.updatePendingRequest(pendingRequest = it)
            pendingRequestAdapter.setRequestList(viewModel.addFriendsUiState.value.pendingRequestList)
        }
    }

    private fun setUiState(addFriendsUiState: AddFriendsUiState) {

        addFriendsUiState.error.let {

        }

        addFriendsUiState.savedUserList.let {
            listUserAdapter.setUserList(it)
        }

        pendingRequestAdapter.setRequestList(viewModel.addFriendsUiState.value.pendingRequestList)

        addFriendsUiState.pendingRequestList.let {
            pendingRequestAdapter.setRequestList(it)
        }

        addFriendsUiState.loading.let {

        }
    }
}