package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentAuthenticateBinding
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.UserAuthModel

class AuthorizationFragment : Fragment() {

    companion object {
        var Bundle.ARG_AUTH_MODE: String? by StringArg
    }

    private val userAuthViewModel: UserAuthModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAuthenticateBinding.inflate(
            inflater,
            container,
            false
        )

        "student".let(binding.login::setText)
        "secret".let(binding.password::setText)

        arguments?.let{
                if (("login") == it.ARG_AUTH_MODE.toString()){
                    binding.authDialogText.isVisible = false
                    binding.authDialogYes.isVisible = false
                    binding.authDialogNo.isVisible = false

                } else {
                    binding.authDialogText.isVisible = true
                    binding.authDialogYes.isVisible = true
                    binding.authDialogNo.isVisible = true
                    binding.enter.isVisible = false
                    binding.loginTitle.isVisible = false
                    binding.login.isVisible = false
                    binding.passwordTitle.isVisible = false
                    binding.password.isVisible = false
                }
            }

            userAuthViewModel.authData.observe(viewLifecycleOwner){
            AppAuth.getInstance().setUser(AuthModel(it.id,it.token))
            findNavController().navigateUp()
        }

        binding.authDialogYes.setOnClickListener{
            AppAuth.getInstance().removeUser()
            findNavController().navigateUp()
        }

        binding.authDialogNo.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.enter.setOnClickListener {
            userAuthViewModel.authenticate("student","secret")
        }
        return binding.root
    }
}