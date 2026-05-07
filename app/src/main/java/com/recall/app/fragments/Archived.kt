package com.recall.app.fragments

import com.recall.app.R

class Archived : NotallyFragment() {

    override fun getBackground() = R.drawable.archive

    override fun getObservable() = model.archivedNotes
}
