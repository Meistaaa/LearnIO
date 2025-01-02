package com.shazycode.learnio.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shazycode.learnio.R
import com.shazycode.learnio.model.data.Lecture
import com.shazycode.learnio.ui.viewModels.CourseViewModel
import com.shazycode.learnio.ui.viewModels.EnrolledCoursesViewModel

class LectureAdapter(
    private val courseId: String,  // Pass course ID to identify which course we are working with
    private val lectures: MutableList<Lecture>,  // Mutable list to allow changes
    private val onLectureClick: (Lecture) -> Unit // To trigger progress update
) : RecyclerView.Adapter<LectureAdapter.LectureViewHolder>() {



    inner class LectureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lectureTitle: TextView = itemView.findViewById(R.id.lectureTitle)
        private val lectureCheckbox: CheckBox = itemView.findViewById(R.id.lectureCheckbox)
        val enrolledCoursesViewModel:EnrolledCoursesViewModel=EnrolledCoursesViewModel()

        fun bind(lecture: Lecture) {
            lectureTitle.text = lecture.title
            lectureCheckbox.isChecked = lecture.isCompleted

            // Handle checkbox state change
            lectureCheckbox.setOnCheckedChangeListener { _, isChecked ->
                lecture.isCompleted = isChecked  // Update local state
                // Call ViewModel method to update Firestore
                enrolledCoursesViewModel.updateEnrolledLectureCompletion(courseId, lecture.title, isChecked)
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
}
