package com.devilishtruthstare.scribereader.mian.anki

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.dictionary.ui.DictionaryView

class AnkiFragment : Fragment(R.layout.fragment_anki) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FrameLayout>(R.id.anki_container).addView(DictionaryView(requireContext()))
    }
}