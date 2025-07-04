package com.devilishtruthstare.scribereader.editor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.databinding.EditorActivityBinding
import com.devilishtruthstare.scribereader.editor.actions.FileInputAction
import com.devilishtruthstare.scribereader.editor.actions.JsonPrettyAction
import com.devilishtruthstare.scribereader.ui.tabview.TabView

class EditorActivity : AppCompatActivity() {
    private val binding get() = _binding!!
    private var _binding: EditorActivityBinding? = null

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = EditorActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        uri = intent.data ?: return

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                initializeEditor()
                initializeTabBar()
            }
            else -> {
                return
            }
        }
    }

    private fun initializeTabBar() {
        val tabView = TabView(this).apply {
            setOnNewTabClick {
                showInputDialog(this@EditorActivity, { input ->

                }, {
                    Toast.makeText(this@EditorActivity, "Operation Canceled", Toast.LENGTH_SHORT).show()
                })
            }
        }

        val fileInput = FileInputAction(this, "Raw Data", uri!!, ::setText)
        tabView.addTab(fileInput)

        val jsonPrettifier = JsonPrettyAction("Pretty JSON", fileInput, ::setText)
        tabView.addTab(jsonPrettifier)

        binding.container.addView(tabView)

        fileInput.onSelected()
    }

    private fun setText(text: String) {
        binding.editorMain.setText(text)
    }

    private fun initializeEditor() {
        binding.editorMain.viewTreeObserver.addOnGlobalLayoutListener {
            val layout = binding.editorMain.layout ?: return@addOnGlobalLayoutListener
            val text = binding.editorMain.text.toString()
            val lineNumbers = StringBuilder()
            var lineIndex = 1

            for (i in 0 until layout.lineCount) {
                val start = layout.getLineStart(i)
                val end = layout.getLineEnd(i)
                if (text.getOrNull(end - 1) == '\n' || end == text.length) {
                    lineNumbers.append("$lineIndex\n")
                    lineIndex++
                } else {
                    lineNumbers.append("\n") // soft-wrapped line
                }
            }

            binding.lineNumbers.text = lineNumbers.toString()
        }

        binding.editorMain.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val layout = binding.editorMain.layout ?: return
                val text = s.toString()
                val lineNumbers = StringBuilder()
                var lineIndex = 1

                for (i in 0 until layout.lineCount) {
                    val end = layout.getLineEnd(i)
                    if (text.getOrNull(end - 1) == '\n' || end == text.length) {
                        lineNumbers.append("$lineIndex\n")
                        lineIndex++
                    } else {
                        val digits = lineIndex.toString().length
                        lineNumbers.append(" ".repeat(digits) + "\n")
                    }
                }

                binding.lineNumbers.text = lineNumbers.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showInputDialog(context: Context, onSubmit: (String) -> Unit, onCancel: () -> Unit) {
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(context)
            .setTitle("Enter New Tab Name")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                onSubmit(input.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                onCancel()
            }
            .setOnCancelListener {
                onCancel()
            }
            .show()

        input.post {
            input.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_FORCED)
        }

    }
}