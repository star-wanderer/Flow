package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentUnauthenticateBinding
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.viewmodel.UserAuthModel
class UnAuthorizationFragment : Fragment() {

    private val userAuthViewModel: UserAuthModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUnauthenticateBinding.inflate(
            inflater,
            container,
            false
        )
        userAuthViewModel.authData.observe(viewLifecycleOwner){ authModel ->
            AppAuth.getInstance().setUser(AuthModel(authModel.id,authModel.token))
        findNavController().navigateUp()
        }

        binding.authDialogYes.setOnClickListener{
            AppAuth.getInstance().removeUser()
            findNavController().navigateUp()
        }

        binding.authDialogNo.setOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }
}