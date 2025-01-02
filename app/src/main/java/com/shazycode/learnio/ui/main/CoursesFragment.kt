package com.shazycode.learnio.ui.main

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.shazycode.learnio.databinding.FragmentCoursesBinding
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.model.data.Lecture
import com.shazycode.learnio.model.repositories.AuthRepository
import com.shazycode.learnio.ui.main.adapters.CourseAdapter
import com.shazycode.learnio.ui.main.adapters.TemporaryLectureAdapter
import com.shazycode.learnio.ui.viewModels.CourseViewModel
import com.shazycode.learnio.ui.viewModels.EnrolledCoursesViewModel
import kotlinx.coroutines.launch

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels()
    private var uri: Uri? = null // To hold the image URI
    private val enrolledCoursesViewModel: EnrolledCoursesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var lectureAdapter: TemporaryLectureAdapter // Create an adapter for the lectures
    private val lectures = mutableListOf<Lecture>() // List to hold lectures

    private val PERMISSION_REQUEST_CODE = 101

    // Register for the gallery result using the new ActivityResult API
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            uri = result.data?.data
            if (uri != null) {
                binding.courseImageView.visibility = View.VISIBLE // Make the ImageView visible
                binding.courseImageView.setImageURI(uri) // Set the selected image to ImageView
            } else {
                Log.e("Gallery", "No image selected")
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView for lectures
        lectureAdapter = TemporaryLectureAdapter(
            lectures,
            onLectureClick = { lecture ->
                // Handle lecture click (update progress or perform other actions)
            }
        )
        // Set up RecyclerView
        binding.recyclerViewLectures.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lectureAdapter
        }

        // Add lecture button click listener
        binding.addLectureButton.setOnClickListener {
            addLecture() // Function to add a new lecture
        }

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

        // Spinner options setup
        val courseLevels = listOf("Beginner", "Intermediate", "Advanced")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.courseLevelSpinner.adapter = adapter

        // Check for permissions when selecting an image from gallery
        checkPermission()

        // Show/Hide UI elements based on user role
        setupUserInterface()

        // Add course button click listener
        binding.addCourseButton.setOnClickListener {
            addCourse()
        }

        // Bind image selection to the button, not the ImageView
        binding.selectImageButton.setOnClickListener {
            chooseImageFromGallery()
        }
    }
    // Add a function to add a lecture
    private fun addLecture() {
        val lectureTitle = binding.lectureTitleEditText.text.toString() // Assume you have an EditText for lecture titles
        if (lectureTitle.isNotBlank()) {
            lectures.add(Lecture(title = lectureTitle))
            lectureAdapter.notifyItemInserted(lectures.size - 1)
            binding.lectureTitleEditText.text.clear() // Clear input field
            updateProgress()
        } else {
            Toast.makeText(context, "Lecture title cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update progress
    private fun updateProgress() {
        val completedCount = lectures.count { it.isCompleted }
        // Update the course object here if needed or notify the user about the progress
        // Example: Toast.makeText(context, "Progress: $completedCount/${lectures.size}", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.courseImageView.setOnClickListener {
                chooseImageFromGallery()
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupUserInterface() {
        val user: FirebaseUser = AuthRepository().getCurrentUser()!!
        val isAdmin = user.email == "chshahzaibakhtar177@gmail.com"

        if (isAdmin) {
            binding.addCourseButton.visibility = View.VISIBLE
            binding.courseTitleEditText.visibility = View.VISIBLE
            binding.courseDescriptionEditText.visibility = View.VISIBLE
          
            binding.courseContentEditText.visibility = View.VISIBLE
            binding.courseStatusSwitch.visibility = View.VISIBLE
            binding.courseLevelSpinner.visibility = View.VISIBLE
            binding.courseTagsEditText.visibility = View.VISIBLE
            binding.courseDurationEditText.visibility = View.VISIBLE
            binding.courseInstructorEditText.visibility = View.VISIBLE
            binding.addLectureButton.visibility = View.VISIBLE
            binding.lectureTitleEditText.visibility=View.VISIBLE
        } else {
            binding.addCourseButton.visibility = View.GONE
            binding.courseTitleEditText.visibility = View.GONE
            binding.courseDescriptionEditText.visibility = View.GONE
            binding.courseImageView.visibility = View.GONE
            binding.courseContentEditText.visibility = View.GONE
            binding.courseStatusSwitch.visibility = View.GONE
            binding.courseLevelSpinner.visibility = View.GONE
            binding.courseTagsEditText.visibility = View.GONE
            binding.courseDurationEditText.visibility = View.GONE
            binding.courseInstructorEditText.visibility = View.GONE
            binding.addLectureButton.visibility = View.GONE
            binding.lectureTitleEditText.visibility=View.GONE

            lifecycleScope.launchWhenStarted {
                enrolledCoursesViewModel.enrolledCourses.collect { courses ->
                    courseAdapter.submitList(courses)
                }
            }
        }
    }

    private fun addCourse() {
        val courseTitle = binding.courseTitleEditText.text.toString()
        val courseDescription = binding.courseDescriptionEditText.text.toString()
        val courseContent = binding.courseContentEditText.text.toString()
        val courseStatus = binding.courseStatusSwitch.isChecked
        val courseLevel = binding.courseLevelSpinner.selectedItem.toString()
        val courseTags = binding.courseTagsEditText.text.toString()
        val courseDuration = binding.courseDurationEditText.text.toString()
        val courseInstructor = binding.courseInstructorEditText.text.toString()
        val lecturesList = lectures.map { Lecture(it.title, it.isCompleted) } // Assuming lectures is a list of Lecture objects


        if (courseTitle.isNotBlank() && courseDescription.isNotBlank()) {
            binding.addCourseButton.isEnabled = false

            val newCourse = Course().apply {
                this.courseTitle = courseTitle
                this.courseDescription = courseDescription
                this.dateAdded = System.currentTimeMillis().toString()
                this.status = courseStatus
                this.courseLevel = courseLevel
                this.tags = courseTags.split(",").map { it.trim() }
                this.courseDuration = courseDuration
                this.courseInstructor = courseInstructor
                this.courseContent = courseContent.split(",").map { it.trim() }
                this.courseImage = uri.toString() // Store the URI directly
                this.lectures = lecturesList

            }

            // If image is selected, upload it and then add the course
            if (uri != null) {
                courseViewModel.addCourseWithImage(uri!!, newCourse) // Pass the URI directly
                Toast.makeText(context, "Course Added with Image Successfully", Toast.LENGTH_SHORT).show()
            } else {
                // If no image, simply add the course without an image
                addCourseWithoutImage(newCourse)
                Toast.makeText(context, "Course Added Without Image Successfully", Toast.LENGTH_SHORT).show()
            }

            // Clear the input fields
            clearInputFields()
        } else {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to choose image from gallery
    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Function to add course without an image
    private fun addCourseWithoutImage(course: Course) {
        lifecycleScope.launch {
            try {
                val result = courseViewModel.addCourse(course)
                if (result.isSuccess) {
                    Toast.makeText(context, "Course Added Without Image", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error Adding Course", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.addCourseButton.isEnabled = true
            }
        }
    }

    // Function to clear input fields
    private fun clearInputFields() {
        binding.courseTitleEditText.text.clear()
        binding.courseDescriptionEditText.text.clear()
        binding.courseStatusSwitch.isChecked = false
        binding.courseLevelSpinner.setSelection(0)
        binding.courseTagsEditText.text.clear()
        binding.courseDurationEditText.text.clear()
        binding.courseInstructorEditText.text.clear()
        binding.courseContentEditText.text.clear()
        binding.courseImageView.setImageURI(null) // Clear the image preview
        binding.lectureTitleEditText.text.clear() // Clear the lecture title input
        lectures.clear() // Clear the lectures list if necessary
        lectureAdapter.notifyDataSetChanged()
        uri = null // Reset the URI
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with selecting an image
                    chooseImageFromGallery()
                } else {
                    // Permission denied, inform the user
                    Toast.makeText(context, "Permission denied. Unable to choose image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}









