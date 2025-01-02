package com.shazycode.learnio.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.shazycode.learnio.databinding.ItemCourseBinding
import com.shazycode.learnio.model.data.Course
import com.shazycode.learnio.ui.main.viewHolders.CoursesViewHolder

class CourseAdapter(private val onItemClicked: (Course) -> Unit) : ListAdapter<Course, CoursesViewHolder>(
    CourseDiffCallback()
) {

    // DiffUtil callback for efficient updates
    class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id // Compare IDs!
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem // Or use a specific comparison if needed
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoursesViewHolder(binding) // Use binding instead of view
    }

    override fun onBindViewHolder(holder: CoursesViewHolder, position: Int) {
        val course = getItem(position) // Use getItem() here!
        holder.bind(course)
        holder.itemView.setOnClickListener { onItemClicked(course) }
    }
}
