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
import com.shazycode.learnio.databinding.FragmentEnrolledCoursesBinding
import com.shazycode.learnio.ui.main.adapters.CourseAdapter
import com.shazycode.learnio.ui.viewModels.EnrolledCoursesViewModel
class EnrolledCourses : Fragment() {

    private var _binding: FragmentEnrolledCoursesBinding? = null
    private val binding get() = _binding!!
    private val enrolledCoursesViewModel: EnrolledCoursesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnrolledCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        courseAdapter = CourseAdapter { course ->
            val intent = Intent(requireContext(), CourseDetailsActivity::class.java).apply {
                putExtra("course_data", course)
            }
            startActivity(intent)
        }
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }

        // Observe enrolled courses and update the adapter
        lifecycleScope.launchWhenStarted {
            enrolledCoursesViewModel.enrolledCourses.collect { enrolledCourses ->
                courseAdapter.submitList(enrolledCourses)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
