package com.shazycode.learnio.ui.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shazycode.learnio.model.repositories.CourseRepository
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.model.repositories.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CourseViewModel : ViewModel() {

    private val courseRepository = CourseRepository()
    private val storageRepository = StorageRepository()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    private val _courseState = MutableStateFlow<Course?>(null)
    val courseState: StateFlow<Course?> = _courseState


    init {
        getCourses()
    }


    // Update lecture completion status
    fun updateLectureCompletion(courseId: String, lectureTitle: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val result = courseRepository.updateLectureCompletionStatus(courseId, lectureTitle, isCompleted)
                if (result.isSuccess) {
                    Log.d("CourseViewModel", "Lecture completion status updated successfully")
                } else {
                    Log.e("CourseViewModel", "Failed to update lecture completion status")
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating lecture completion", e)
            }
        }
    }

//    fun getCourseData(courseId: String) {
//        viewModelScope.launch {
//            try {
//                val course = courseRepository.getCourseById(courseId)  // Fetch course data from Firestore
//                if (course != null) {
//                    // Update the local state or trigger an update in the activity
//                    _courseState.value = course  // Assuming you have a _courseState MutableStateFlow in the ViewModel
//                }
//            } catch (e: Exception) {
//                Log.e("CourseViewModel", "Error fetching course data", e)
//            }
//        }
//    }



    private fun getCourses() {
        viewModelScope.launch {
            courseRepository.getCourses().collect { courseList ->
                _courses.value = courseList
                Log.d("CourseViewModel", "Received ${courseList.size} courses:")
                courseList.forEach { course ->
                    Log.d("CourseViewModel", "Course: ${course.courseTitle}, ID: ${course.id}")
                }
            }
        }
    }

    suspend fun updateCourse(course: Course): Result<Boolean> {
        return try {
            val result = courseRepository.updateCourse(course)
            if (result.isSuccess) {
                getCourses() // Refresh course list
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add course with image handling in a coroutine

    fun addCourseWithImage(imageUri: Uri, course: Course) {
        viewModelScope.launch {
            try {
                val imageUrl = uploadImage(imageUri)
                course.courseImage = imageUrl

                addCourse(course)
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error adding course with image: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String {
        return suspendCancellableCoroutine { continuation ->
            storageRepository.uploadFile(imageUri) { success, result ->
                if (success) {
                    continuation.resume(result!!)
                } else {
                    continuation.resumeWithException(Exception("Image upload failed: $result"))
                }
            }
        }
    }



    // Add course method
    suspend fun addCourse(course: Course): Result<Boolean> {
        return try {
            val result = courseRepository.addCourse(course)
            if (result.isSuccess) {
                getCourses() // Refresh course list
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to add course"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
