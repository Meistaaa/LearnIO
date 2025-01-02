package com.shazycode.learnio.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.shazycode.learnio.R
import com.shazycode.learnio.databinding.ActivityCourseDetailsBinding
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.model.data.Lecture
import com.shazycode.learnio.model.repositories.AuthRepository
import com.shazycode.learnio.ui.main.adapters.LectureAdapter
import com.shazycode.learnio.ui.viewModels.CourseViewModel
import com.shazycode.learnio.ui.viewModels.EnrolledCoursesViewModel
import kotlinx.coroutines.launch
class CourseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailsBinding
    private val enrolledCoursesViewModel: EnrolledCoursesViewModel by viewModels()

    private var isEnrolled = false
    private lateinit var course: Course
    private val user: FirebaseUser = AuthRepository().getCurrentUser()!!
    private val studentId = user.email.toString()
    private lateinit var lectureAdapter: LectureAdapter
    private val courseViewModel: CourseViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        course = intent.getParcelableExtra<Course>("course_data")!!

        if (::course.isInitialized) {

            setupCourseDetails()
            setupLectureRecyclerView() // Set up RecyclerView for lectures
            checkEnrollmentStatus()

            binding.enrollCourseButton.setOnClickListener {
                handleEnrollment()
            }

//            binding.editCourseButton.setOnClickListener {
//                Toast.makeText(this, "Editing ${course.courseTitle}", Toast.LENGTH_SHORT).show()
//            }
        } else {
            Toast.makeText(this, "Error: Course data not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupCourseDetails() {
        binding.courseTitleTextView.text = course.courseTitle
        binding.courseDescriptionTextView.text = course.courseDescription
        binding.courseInstructorTextView.text = "Instructor: ${course.courseInstructor}"
        binding.courseDurationTextView.text = "Duration: ${course.courseDuration}"
        binding.courseTagsTextView.text = "Tags: ${course.tags?.joinToString(", ")}"
        binding.courseStatusTextView.text = "Status: ${course.status}"
        binding.courseContentTextView.text = "Topics: ${course.courseContent?.joinToString(", ")}"

        Glide.with(this)
            .load(course.courseImage)
            .error(R.drawable.error_image)
            .placeholder(R.drawable.placeholder)
            .into(binding.courseImageView)
    }

    private fun updateEnrolledLectureProgress(lecture: Lecture) {
        enrolledCoursesViewModel.updateEnrolledLectureCompletion(
            courseId = course.id,
            lectureTitle = lecture.title,
            isCompleted = lecture.isCompleted
        )
        updateProgress()
    }


    private fun setupLectureRecyclerView() {
        // Use a mutable list so it can be updated dynamically
        lectureAdapter = LectureAdapter(course.id, course.lectures.toMutableList()) { lecture ->
//            updateEnrolledLectureProgress(lecture)
            // Recalculate progress whenever a lecture is checked/unchecked
            updateProgress()
        }

        binding.recyclerViewLectures.apply {
            adapter = lectureAdapter
            layoutManager = LinearLayoutManager(this@CourseDetailsActivity)
        }

        // Show lectures only if the user is enrolled
        binding.recyclerViewLectures.visibility = if (isEnrolled) View.VISIBLE else View.GONE
        updateProgress()
    }



    private fun updateProgress() {
        // Count the number of completed lectures
        val completedCount = course.lectures.count { it.isCompleted }

        // Calculate progress percentage based on the number of completed lectures
        val progressPercentage = (completedCount.toFloat() / course.lectures.size * 100).toInt()

        // Update the progress bar
        binding.progressBar.progress = progressPercentage

        // Optionally, show progress in a Toast message
        Toast.makeText(this, "Progress: $completedCount/${course.lectures.size} (${progressPercentage}%)", Toast.LENGTH_SHORT).show()
    }


    private fun checkEnrollmentStatus() {
        enrolledCoursesViewModel.isCourseEnrolled(course.id) // Check if the course is enrolled

        // Collecting the enrollment status in a coroutine
        lifecycleScope.launch {
            enrolledCoursesViewModel.isEnrolled.collect { enrolled ->
                isEnrolled = enrolled
                updateEnrollButtonText()
                binding.recyclerViewLectures.visibility = if (isEnrolled) View.VISIBLE else View.GONE // Show lectures if enrolled
                binding.progressBar.visibility = if (isEnrolled) View.VISIBLE else View.GONE // Show progress bar if enrolled
            }
        }
    }

    private fun handleEnrollment() {
        lifecycleScope.launch {
            if (!isEnrolled) {
                enrolledCoursesViewModel.addEnrollment(studentId, course)
                binding.recyclerViewLectures.visibility = View.VISIBLE
                Toast.makeText(this@CourseDetailsActivity, "Enrolled in and you can goes to Enrolled Section to Manage lecture ${course.courseTitle}", Toast.LENGTH_SHORT).show()
            } else {
                enrolledCoursesViewModel.unenrollStudentFromCourse(studentId, course.id)
                binding.recyclerViewLectures.visibility=View.GONE
                Toast.makeText(this@CourseDetailsActivity, "Unenrolled from ${course.courseTitle}", Toast.LENGTH_SHORT).show()
            }
            isEnrolled = !isEnrolled // Toggle enrollment status
            updateEnrollButtonText()
        }
    }

    private fun updateEnrollButtonText() {
        binding.enrollCourseButton.text = if (isEnrolled) "Unenroll" else "Enroll"
    }
    // Helper function to get the button text based on enrollment status
    private fun getEnrollButtonText():String{
        return if (isEnrolled) "Unenroll" else "Enroll"
    }
}
