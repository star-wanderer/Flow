package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.databinding.FragmentViewPostBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ViewPostFragment : Fragment() {

    companion object {
        var Bundle.ARG_POST_ID: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentViewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.let { args ->

            val postId = args.ARG_POST_ID.toString().toLong()

            viewModel.data.observe(viewLifecycleOwner) {
                it.posts.lastOrNull { it.id == postId }?.let { post ->
                    binding.image.load("${BuildConfig.BASE_URL}/media/${post.attachment?.url}")
                }
            }
        }
        return binding.root
    }
}