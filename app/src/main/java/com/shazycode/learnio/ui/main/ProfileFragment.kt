package com.shazycode.learnio.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.shazycode.learnio.databinding.FragmentProfileBinding
import com.shazycode.learnio.ui.auth.LoginActivity
import com.shazycode.learnio.ui.viewModels.AuthViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Using AuthViewModel instead of ProfileViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the current user from the AuthViewModel
        lifecycleScope.launchWhenStarted {
            authViewModel.currentUser.collect { user ->
                if (user != null) {
                    binding.userNameTextView.text = user.displayName ?: "No Name"
                    binding.userEmailTextView.text = user.email ?: "No Email"
                } else {
                    // Navigate to LoginActivity if user is null
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
        }

        // Logout Button Click Listener
        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
