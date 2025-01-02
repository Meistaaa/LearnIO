package com.shazycode.learnio.model.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolledCourse(
    var id: String = "",
    var course: Course=Course(), // Reference to the course object
    var dateEnrolledCourse: String="", // Enrollment date or any other relevant date
    var studentId: String = "", // You can add student ID or other student-specific data
    var status: String = "", // Enrollment status (e.g., active, completed)
) : Parcelable
