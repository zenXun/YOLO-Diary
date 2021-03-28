package com.example.yolo_diary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*

private const val TAG = "DiaryListFragment"

class DiaryListFragment : Fragment() {

    interface Callbacks {
        fun onDiaryClicked(diaryId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var diaryRecyclerView: RecyclerView
    private var adapter: DiaryAdapter? = DiaryAdapter(emptyList())

    private val diaryListViewModel: DiaryListViewModel by lazy {
        ViewModelProviders.of(this).get(DiaryListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_list, container, false)

        diaryRecyclerView = view.findViewById(R.id.diary_recycler_view) as RecyclerView
        diaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this.adapter
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diaryListViewModel.diariesListLiveData.observe(
            viewLifecycleOwner,
            Observer { diaries ->
                diaries?.let {
                    Log.i(TAG, "Got diaries ${diaries.size}")
                }
                updateUI(diaries)
            }
        )
    }

    private fun updateUI(diaries: List<Diary>) {
        adapter = DiaryAdapter(diaries)
        diaryRecyclerView.adapter = adapter
    }

    private inner class DiaryHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var diary: Diary

        private val titleTextView: TextView = itemView.findViewById(R.id.diary_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.diary_date)
        private val completedImageView: ImageView = itemView.findViewById(R.id.diary_completed_image_view)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(diary: Diary) {
            this.diary = diary
            titleTextView.text = this.diary.title
            dateTextView.text = DateFormat.getDateInstance().format(this.diary.date)
            completedImageView.visibility = if (diary.completed) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(p0: View?) {
            callbacks?.onDiaryClicked(this.diary.id)
        }
    }

    private inner class DiaryAdapter(var diaries: List<Diary>): RecyclerView.Adapter<DiaryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryHolder =
            DiaryHolder(layoutInflater.inflate(R.layout.list_item_diary, parent, false))

        override fun onBindViewHolder(holder: DiaryHolder, position: Int) =
            holder.bind(diaries[position])

        override fun getItemCount() = diaries.size

    }

    companion object {
        fun newInstance(): DiaryListFragment{
            return DiaryListFragment()
        }
    }
}