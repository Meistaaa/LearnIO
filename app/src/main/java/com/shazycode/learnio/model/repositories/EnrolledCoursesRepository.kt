package com.shazycode.learnio.model.repositories

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.model.data.EnrolledCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EnrolledCoursesRepository {
    private val enrolledCourseDocument = FirebaseFirestore.getInstance().collection("enrolledCourses")
    private val coursesCollection = FirebaseFirestore.getInstance().collection("courses")

    suspend fun updateEnrolledLectureCompletionStatus(
        studentId: String,
        courseId: String,
        lectureTitle: String,
        isCompleted: Boolean
    ): Result<Boolean> {
        return try {
            // Query to find the enrolled course for the student
            val enrolledCourseSnapshot = enrolledCourseDocument
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("course.id", courseId)
                .get()
                .await()

            if (enrolledCourseSnapshot.isEmpty) {
                return Result.failure(Exception("Enrolled course not found for the given student"))
            }

            // Get the enrolled course document reference and object
            val enrolledDocument = enrolledCourseSnapshot.documents.first()
            val enrolledCourse = enrolledDocument.toObject(EnrolledCourse::class.java)

            if (enrolledCourse != null) {
                // Update the specific lecture's completion status
                val updatedLectures = enrolledCourse.course.lectures.map { lecture ->
                    if (lecture.title == lectureTitle) {
                        lecture.isCompleted = isCompleted
                    }
                    lecture
                }

                // Update the lectures and completed lectures count in the enrolled course
                enrolledCourse.course.lectures = updatedLectures
                enrolledCourse.course.completedLectures = updatedLectures.count { it.isCompleted }

                // Save the updated enrolled course back to Firestore
                enrolledDocument.reference.set(enrolledCourse).await()
                Result.success(true)
            } else {
                Result.failure(Exception("Enrolled course data could not be retrieved"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Method to add a new enrollment
    suspend fun addEnrollment(studentId: String, course: Course): Result<Boolean> {
        return try {
            val enrolledCourse = EnrolledCourse(
                id = "",
                course = course,
                studentId = studentId,
                dateEnrolledCourse = System.currentTimeMillis().toString()
            )

            val document = enrolledCourseDocument.document()
            enrolledCourse.id = document.id
            document.set(enrolledCourse).await()

            // Increment enrollment count and set isEnrolled to true for the course
            val courseDocument = coursesCollection.document(course.id)
            courseDocument.update(
                "enrollmentCount", FieldValue.increment(1),
            ).await()

            // Update enrollment count in all enrolledCourses documents with the same course ID
            val enrolledCoursesSnapshot = enrolledCourseDocument
                .whereEqualTo("course.id", course.id)
                .get()
                .await()

            for (enrolledDocument in enrolledCoursesSnapshot.documents) {
                enrolledDocument.reference.update("enrollmentCount", FieldValue.increment(1)).await()
                }


            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Method to get all enrolled courses for a specific student and return a Flow
    fun getEnrolledCoursesForStudent(studentId: String): Flow<List<Course>> {
        return enrolledCourseDocument
            .whereEqualTo("studentId", studentId)  // Query for the student ID
            .snapshots()  // Get a snapshot of the enrolled courses
            .map { querySnapshot ->
                val enrolledCoursesList = querySnapshot.documents.mapNotNull { document ->
                    val enrolledCourse = document.toObject(EnrolledCourse::class.java)
                    enrolledCourse?.course  // Extract the course from the EnrolledCourse object
                }
                enrolledCoursesList  // Return the list of courses
            }
            .catch { e ->
                Log.e("EnrolledCoursesRepository", "Error fetching enrolled courses", e)
                emit(emptyList())  // Emit an empty list in case of error
            }
    }

    // Method to unenroll a student from a course
    suspend fun unenroll(studentId: String, courseId: String): Result<Boolean> {
        return try {
            // Find the enrollment document for the student and course
            val querySnapshot = enrolledCourseDocument
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("course.id", courseId)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // If no enrollment exists, return failure
                return Result.failure(Exception("No enrollment found for this course and student"))
            }

            // Get the document ID of the enrolled course
            val enrolledCourseDocument = querySnapshot.documents.first()
            // Delete the enrollment document from Firestore
            enrolledCourseDocument.reference.delete().await()

            // Update the course: Decrement the enrollment count and set isEnrolled to false if needed
            val courseDocument = coursesCollection.document(courseId)
            courseDocument.update(
                "enrollmentCount", FieldValue.increment(-1),
            ).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to check if the course is enrolled
    suspend fun isCourseEnrolled(courseId: String, studentId: String): Boolean {
        return try {
            // Check if the course is already enrolled by querying the database for the course ID
            val querySnapshot = enrolledCourseDocument
                .whereEqualTo("course.id", courseId)  // Query by course ID
                .whereEqualTo("studentId", studentId)  // Query by student ID
                .get()
                .await()

            // If any document is returned, the course is enrolled
            querySnapshot.isEmpty.not()
        } catch (e: Exception) {
            // In case of any error, return false (not enrolled)
            false
        }
    }


    suspend fun unenrollAllFromCourse(courseId: String, studentId: String): Result<Boolean> {
        return try {
            val querySnapshot = enrolledCourseDocument
            .whereEqualTo("course.id", courseId)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
                }
            Result.success(true)
            } catch (e: Exception) {
            Result.failure(e)
            }
        }
}
