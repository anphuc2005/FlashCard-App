package com.example.flashcardapp.core.utils

import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun TextInputEditText.textChangesFlow(): Flow<String> = callbackFlow {
    val watcher = addTextChangedListener { editable ->
        trySend(editable?.toString().orEmpty())
    }

    awaitClose {
        removeTextChangedListener(watcher)
    }
}
