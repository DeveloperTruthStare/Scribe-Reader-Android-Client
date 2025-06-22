package com.devilishtruthstare.scribereader.ui.anki

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.jmdict.ui.DictionaryView

class AnkiFragment : Fragment(R.layout.fragment_anki) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AnkiFrag", "onViewCreated")
        view.findViewById<FrameLayout>(R.id.anki_container).addView(DictionaryView(requireContext()))
    }
}