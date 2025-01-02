package com.shazycode.learnio.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazycode.learnio.databinding.FragmentHomeBinding
import com.shazycode.learnio.ui.main.adapters.CourseAdapter
import com.shazycode.learnio.ui.viewModels.CourseViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels() // ViewModel instance
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Initialize RecyclerView and Adapter
        courseAdapter = CourseAdapter { course ->
            val intent = Intent(requireContext(), CourseDetailsActivity::class.java).apply {
                putExtra("course_data", course) // Pass the entire Course object
            }
            startActivity(intent)
        }
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }

        // Observe ViewModel's courses and submit to adapter
        lifecycleScope.launchWhenStarted {
            courseViewModel.courses.collect { courses ->
                courseAdapter.submitList(courses)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
