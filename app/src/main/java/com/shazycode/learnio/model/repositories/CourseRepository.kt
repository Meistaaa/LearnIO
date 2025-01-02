package com.shazycode.learnio.model.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.shazycode.learnio.model.data.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await




class CourseRepository {

    private val coursesCollection = FirebaseFirestore.getInstance().collection("courses")
    private val user: FirebaseUser = AuthRepository().getCurrentUser()!!
    private val studentId = user.email.toString()


    // Update a specific lecture's completion status
    suspend fun updateLectureCompletionStatus(courseId: String, lectureTitle: String, isCompleted: Boolean): Result<Boolean> {
        return try {
            // Find the course document
            val courseDocument = coursesCollection.document(courseId)
            val course = courseDocument.get().await().toObject(Course::class.java)

            if (course != null) {
                // Update the specific lecture's completion status
                val updatedLectures = course.lectures.map { lecture ->
                    if (lecture.title == lectureTitle) {
                        lecture.isCompleted = isCompleted
                    }
                    lecture
                }

                // Save the updated course data back to Firestore
                course.lectures = updatedLectures
                courseDocument.set(course).await()
                Result.success(true)
            } else {
                Result.failure(Exception("Course not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add a new course to Firestore
    suspend fun addCourse(course: Course): Result<Boolean> {
        return try {
            val document = coursesCollection.document()
            course.id = document.id
            document.set(course).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    // In CourseRepository.kt
    fun getCourses(): Flow<List<Course>> {
        return coursesCollection.snapshots().map { snapshot ->
            snapshot.documents.forEach { doc ->
                Log.d("CourseRepository", "Document: ${doc.data}") // Log raw data for debugging
            }
            val courseList = snapshot.toObjects(Course::class.java)
            Log.d("CourseRepository", "Fetched courses: ${courseList.size} courses")
            courseList.forEach { Log.d("CourseRepository", "Course: ${it.courseTitle}, ID: ${it.id}") }
            courseList
        }.catch { e ->
            Log.e("CourseRepository", "Error fetching courses", e)
            emit(emptyList())
        }
    }


    // Get a specific course by ID
    suspend fun getCourseById(courseId: String): Result<Course> {
        return try {
            val document = coursesCollection.document(courseId).get().await()
            if (document.exists()) {
                val course = document.toObject(Course::class.java)
                if (course != null) {
                    Result.success(course)
                } else {
                    Result.failure(Exception("Course not found"))
                }
            } else {
                Result.failure(Exception("Course not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increments the enrollment count of a course
    suspend fun incrementEnrollmentCount(courseId: String): Result<Boolean> {
        return try {
            val document = coursesCollection.document(courseId)
            document.update("enrollmentCount", FieldValue.increment(1)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Update an existing course
    suspend fun updateCourse(course: Course): Result<Boolean> {
        return try {
            val document = coursesCollection.document(course.id)
            document.set(course).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a course
    suspend fun deleteCourse(courseId: String): Result<Boolean> {
        return try {
            // Delete the course
            coursesCollection.document(courseId).delete().await()

            // Also delete any enrolled courses related to this course
            val enrolledCoursesRepository = EnrolledCoursesRepository()
            enrolledCoursesRepository.unenrollAllFromCourse(courseId=courseId, studentId = studentId)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
