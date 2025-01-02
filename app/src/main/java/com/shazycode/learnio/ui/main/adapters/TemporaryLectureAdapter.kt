package com.shazycode.learnio.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shazycode.learnio.R
import com.shazycode.learnio.model.data.Lecture

class TemporaryLectureAdapter(
    private val lectures: MutableList<Lecture>,  // Mutable list to allow changes
    private val onLectureClick: (Lecture) -> Unit
) : RecyclerView.Adapter<TemporaryLectureAdapter.LectureViewHolder>() {

    inner class LectureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lectureTitle: TextView = itemView.findViewById(R.id.lectureTitle)
        private val lectureCheckbox: CheckBox = itemView.findViewById(R.id.lectureCheckbox)

        fun bind(lecture: Lecture) {
            lectureTitle.text = lecture.title
            lectureCheckbox.isChecked = lecture.isCompleted

            // Handle checkbox state change
            lectureCheckbox.setOnCheckedChangeListener { _, isChecked ->
                lecture.isCompleted = isChecked  // Update lecture completion status
                onLectureClick(lecture)  // Notify activity to update progress bar
            }

            itemView.setOnClickListener {
                onLectureClick(lecture)  // Notify activity to update progress bar when lecture clicked
                notifyItemChanged(adapterPosition)  // Update the item view
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LectureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lecture, parent, false)
        return LectureViewHolder(view)
    }

    override fun onBindViewHolder(holder: LectureViewHolder, position: Int) {
        holder.bind(lectures[position])
    }

    override fun getItemCount(): Int = lectures.size

    // Add a method to temporarily update the lecture list
    fun updateLectures(newLectures: List<Lecture>) {
        lectures.clear()  // Clear the current list
        lectures.addAll(newLectures)  // Add the new list
        notifyDataSetChanged()  // Notify adapter that the data has changed
    }
}
