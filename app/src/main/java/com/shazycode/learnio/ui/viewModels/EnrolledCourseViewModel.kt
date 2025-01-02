package com.shazycode.learnio.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.model.repositories.AuthRepository
import com.shazycode.learnio.model.repositories.EnrolledCoursesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnrolledCoursesViewModel : ViewModel() {

    private val enrolledCoursesRepository = EnrolledCoursesRepository()

    private val _enrolledCourses = MutableStateFlow<List<Course>>(emptyList())
    val enrolledCourses: StateFlow<List<Course>> = _enrolledCourses

    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> = _isEnrolled





    private val user:FirebaseUser=AuthRepository().getCurrentUser()!!
    private val studentId=user.email.toString()
    init {
        getEnrolledCourses(studentId = studentId)
    }


    // Update lecture completion status for an enrolled course
    fun updateEnrolledLectureCompletion( courseId: String, lectureTitle: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val result = enrolledCoursesRepository.updateEnrolledLectureCompletionStatus(studentId, courseId, lectureTitle, isCompleted)
                if (result.isSuccess) {
                    Log.d("CourseViewModel", "Enrolled course lecture completion status updated successfully")
                } else {
                    Log.e("CourseViewModel", "Failed to update enrolled course lecture completion status")
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating enrolled course lecture completion", e)
            }
        }
    }


    // Function to check if a course is enrolled
    fun isCourseEnrolled(courseId: String) {
        viewModelScope.launch {
            // Use the repository to check if the course is enrolled
            val result = enrolledCoursesRepository.isCourseEnrolled(courseId, studentId)
            _isEnrolled.value = result
        }
    }

    // Fetch enrolled courses for a student
    fun getEnrolledCourses(studentId: String) {
        viewModelScope.launch {
            try {
                // Fetch the list of enrolled courses from the repository
                enrolledCoursesRepository.getEnrolledCoursesForStudent(studentId).collect { courseList ->
                    _enrolledCourses.value = courseList
                    Log.d("EnrolledCoursesViewModel", "Fetched ${courseList.size} enrolled courses.")
                    courseList.forEach { course ->
                        Log.d("EnrolledCoursesViewModel", "Course: ${course.courseTitle}, ID: ${course.id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("EnrolledCoursesViewModel", "Error fetching enrolled courses: ${e.localizedMessage}")
            }
        }
    }

    // Add a course to the enrolled courses
    fun addEnrollment(studentId: String, course: Course) {
        viewModelScope.launch {
            try {
                // Enroll the student in the course
                val result = enrolledCoursesRepository.addEnrollment(studentId, course)
                if (result.isSuccess) {
                    Log.d("EnrolledCoursesViewModel", "Successfully enrolled in course: ${course.courseTitle}")
                    getEnrolledCourses(studentId) // Refresh the enrolled courses list
                } else {
                    Log.e("EnrolledCoursesViewModel", "Failed to enroll in course: ${course.courseTitle}")
                }
            } catch (e: Exception) {
                Log.e("EnrolledCoursesViewModel", "Error adding enrollment: ${e.localizedMessage}")
            }
        }
    }

    // Unenroll a student from a course
    fun unenrollStudentFromCourse(studentId: String, courseId: String) {
        viewModelScope.launch {
            try {
                // Unenroll the student from the course
                val result = enrolledCoursesRepository.unenroll(studentId, courseId)
                if (result.isSuccess) {
                    Log.d("EnrolledCoursesViewModel", "Successfully unenrolled from course ID: $courseId")
                    getEnrolledCourses(studentId) // Refresh the enrolled courses list
                } else {
                    Log.e("EnrolledCoursesViewModel", "Failed to unenroll from course ID: $courseId")
                }
            } catch (e: Exception) {
                Log.e("EnrolledCoursesViewModel", "Error unenrolling from course: ${e.localizedMessage}")
            }
        }
    }
}
