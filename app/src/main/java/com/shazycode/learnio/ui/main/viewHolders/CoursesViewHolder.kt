package com.shazycode.learnio.ui.main.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.shazycode.learnio.R
import com.shazycode.learnio.databinding.ItemCourseBinding
import com.shazycode.learnio.model.data.Course

class CoursesViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {

    // Helper function to bind data
    fun bind(course: Course) {
        // Bind text views
        binding.courseTitleTextView.text = course.courseTitle
        binding.courseDescriptionTextView.text = course.courseDescription
        binding.courseInstructorTextView.text = "Instructor: ${course.courseInstructor}"
        binding.courseDurationTextView.text = "Duration: ${course.courseDuration}"
        binding.courseTagsTextView.text = "Tags: ${course.tags}"
        binding.enrollmentCountTextView.text = "Enrolled Students: ${course.enrollmentCount}"
        binding.courseStatusTextView.text = "Status: ${course.status}"

        // Bind image (example using Glide for image loading)
        Glide.with(binding.root.context)
            .load(course.courseImage)  // Assuming `course.courseImage` is either a URL or a resource ID
            .placeholder(R.drawable.placeholder)  // Optional: Set a placeholder image while loading
            .error(R.drawable.error_image)  // Optional: Set an error image if loading fails
            .transition(DrawableTransitionOptions.withCrossFade())  // Optional: Add crossfade transition for better UX
            .into(binding.courseImageView)  // Assuming `courseImageView` is the ImageView in your layout
    }
}
