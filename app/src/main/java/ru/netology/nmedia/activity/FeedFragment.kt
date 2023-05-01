package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AuthorizationFragment.Companion.ARG_AUTH_MODE
import ru.netology.nmedia.activity.ViewPostFragment.Companion.ARG_POST_ID
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AppAuthModel
import ru.netology.nmedia.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AppAuthModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onPostImage(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_viewPostFragment,
                    Bundle().apply {
                        ARG_POST_ID = post.id.toString()
                    }
                )
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (!authViewModel.isAuthorized){
                    findNavController().navigate(R.id.action_feedFragment_to_authorizationFragment)
                } else {
                    viewModel.likeById(post.id)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })

        var currentMenuProvider: MenuProvider? = null

        authViewModel.authLiveData.observe(viewLifecycleOwner){

            currentMenuProvider?.let {requireActivity().removeMenuProvider(it)}
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.authorized, authViewModel.isAuthorized)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.isAuthorized)
                }
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId){
                        R.id.signIn -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_authorizationFragment,
                                Bundle().apply { ARG_AUTH_MODE = "login" }
                            )
                            true
                        }
                        R.id.signUp -> {
                            //AppAuth.getInstance().setUser(AuthModel(5, "x-token"))
                            true
                        }
                        R.id.signOut -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_authorizationFragment,
                                Bundle().apply { ARG_AUTH_MODE = "logout" }
                            )
                            true
                        }
                        else -> false
                    }
                }
            }.also { currentMenuProvider = it }, viewLifecycleOwner)
        }

        binding.list.adapter = adapter
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            if (state != 0){
                binding.newPosts.text = context?.resources?.getString(R.string.new_post) + ":" + state
                binding.newPosts.isVisible = true
            }
            println("Retrieved count: $state")
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            println("Cached count: $state")
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        binding.newPosts.setOnClickListener {
            viewModel.updatePosts()
            binding.list.smoothScrollToPosition(0)
            it.isVisible = false
        }

        binding.fab.setOnClickListener {
            if (!authViewModel.isAuthorized){
                findNavController().navigate(R.id.action_feedFragment_to_authorizationFragment)
            }
            else {findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        return binding.root
    }
}
