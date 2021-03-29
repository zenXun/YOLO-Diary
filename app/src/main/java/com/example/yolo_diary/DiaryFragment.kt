package com.example.yolo_diary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.DateFormat
import java.util.*

private const val ARG_DIARY_ID = "diary_id"
private const val TAG = "DiaryFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

class DiaryFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var diary: Diary
    private lateinit var titleField: EditText
    private lateinit var contentField: EditText
    private lateinit var dateButton: Button
    private lateinit var completedCheckBox: CheckBox
    private val diaryDetailViewModel: DiaryDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DiaryDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diary = Diary()
        var diaryId: UUID = arguments?.getSerializable(ARG_DIARY_ID) as UUID
        Log.d(TAG, "args bundle diary ID: $diaryId")
        diaryDetailViewModel.loadDiary(diaryId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)
        titleField = view.findViewById(R.id.diary_title) as EditText
        contentField = view.findViewById(R.id.diary_content) as EditText
        dateButton = view.findViewById(R.id.diary_date) as Button
        completedCheckBox = view.findViewById(R.id.diary_completed) as CheckBox

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diaryDetailViewModel.diaryLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    this.diary = it
                    updateUI()
                }
            }
        )
    }

    fun updateUI() {
        titleField.setText(diary.title)
        contentField.setText(diary.content)
        dateButton.text = DateFormat.getDateInstance().format(diary.date)
        completedCheckBox.apply {
            isChecked = diary.completed
            jumpDrawablesToCurrentState()
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                diary.title = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)

        val contentWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                diary.content = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        }

        contentField.addTextChangedListener(contentWatcher)

        completedCheckBox.apply {
            setOnCheckedChangeListener { _, isCompleted ->
                diary.completed = isCompleted
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(diary.date).apply {
                setTargetFragment(this@DiaryFragment, REQUEST_DATE)
                show(this@DiaryFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        diaryDetailViewModel.saveDiary(diary)
    }

    override fun onDateSelected(date: Date) {
        diary.date = date
        updateUI()
    }

    companion object {

        fun newInstance(diaryId: UUID): DiaryFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DIARY_ID, diaryId)
            }
            return DiaryFragment().apply {
                arguments = args
            }
        }
    }
}