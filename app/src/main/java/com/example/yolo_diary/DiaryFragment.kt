package com.example.yolo_diary

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.text.DateFormat
import java.util.*

private const val ARG_DIARY_ID = "diary_id"
private const val TAG = "DiaryFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_PHOTO = 1

class DiaryFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var diary: Diary
    private lateinit var titleField: EditText
    private lateinit var contentField: EditText
    private lateinit var dateButton: Button
    private lateinit var completedCheckBox: CheckBox
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
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
        photoButton = view.findViewById(R.id.diary_camera) as ImageButton
        photoView = view.findViewById(R.id.diary_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diaryDetailViewModel.diaryLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    this.diary = it
                    photoFile = diaryDetailViewModel.getPhotoFile(it)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.yolo_diary.fileprovider",
                        photoFile)
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
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
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

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(
                captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    Log.d(TAG, "granting uri permission")
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        diaryDetailViewModel.saveDiary(diary)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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