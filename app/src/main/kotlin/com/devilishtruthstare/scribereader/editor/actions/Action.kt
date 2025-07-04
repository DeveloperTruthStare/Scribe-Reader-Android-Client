package com.devilishtruthstare.scribereader.editor.actions

interface Action {
    fun getOutput(): String
    fun addInput(step: Action)
}
