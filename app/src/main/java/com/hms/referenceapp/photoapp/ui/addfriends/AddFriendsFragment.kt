package com.hms.referenceapp.photoapp.ui.addfriends

import android.text.Html
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.hms.referenceapp.photoapp.adapter.ListUserAdapter
import com.hms.referenceapp.photoapp.databinding.FragmentAddFriendsBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.ui.clustered.ClusteredPhotosFragmentArgs
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

    @Inject
    lateinit var listUserAdapter: ListUserAdapter

    override fun setupUi() {
        viewModel.addFriendsUiState.value.let {

        }

        userId = args.userId.toString()

        setAdapter()
        viewModel.getUsers()


        binding.edtSearchUser.addTextChangedListener {
            if (binding.edtSearchUser.text!=null || binding.edtSearchUser.text.isNotEmpty()){
                listUserAdapter.setUserList(viewModel.getFilteredList(binding.edtSearchUser.text.toString()))
            }
        }

        binding.btnAddFriend.setOnClickListener {
            //val userId2 = viewModel.getFilteredList("")[0].user.id.toString()
            //Log.d("firstUIDDDDDDDD",userId)
            //Log.d("secondUIDDDDDDDD",userId2)

            val userList = viewModel.getFilteredList("")

            userList.forEach {
                if (it.isChecked){
                    val userId2 = it.user.id.toString()
                    viewModel.saveUserRelationToCloud(userId,userId2)
                }
            }

            showToast("Friend request sent!!")

            listUserAdapter.setUserList(userList)
        }

        args.userId?.let { viewModel.getPendingRequests(currentUserId = it) }

        viewModel.addFriendsUiState.value.userRelationList.forEach {
            val x = it.areFriends
            val t = it.areFriends
            val v = it.areFriends
        }


    }

    private fun setAdapter(){
        binding.recyclerviewUsers.adapter = listUserAdapter
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.addFriendsUiState, action = ::setUiState)
    }

    private fun setUiState(addFriendsUiState: AddFriendsUiState) {

        addFriendsUiState.error.let {

        }

        addFriendsUiState.savedUserList.let {
            listUserAdapter.setUserList(it)
            //viewModel.getFilteredList("ibra")
        }

        addFriendsUiState.loading.let {

        }
    }
}