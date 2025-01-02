package com.shazycode.learnio.model.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Course(
    var id: String = "",
    var courseTitle: String = "",
    var courseDescription: String = "",
    var courseContent: List<String>? = null,
    var dateAdded: String = "",
    var status: Boolean = false,
    var courseInstructor: String = "",
    var courseDuration: String = "",
    var courseLevel: String = "",
    var courseImage: String? = null,
    var enrollmentCount: Int = 0,
    var tags: List<String>? = null,
    var lectures: List<Lecture> = listOf(),
    var completedLectures: Int = 0,
    var stability: Int = 0
) : Parcelable


@Parcelize
data class Lecture(
    var title: String = "",
    var isCompleted: Boolean = false, // Track completion status
    var stability: Int = 0
) : Parcelable