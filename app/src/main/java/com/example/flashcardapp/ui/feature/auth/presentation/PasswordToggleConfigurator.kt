package com.example.flashcardapp.ui.feature.auth.presentation

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import com.example.flashcardapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

internal fun setupPasswordToggle(
    layout: TextInputLayout,
    editText: TextInputEditText
) {
    var isVisible = false

    fun applyState() {
        val selection = editText.selectionEnd.coerceAtLeast(0)
        editText.transformationMethod = if (isVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        layout.setEndIconDrawable(
            if (isVisible) R.drawable.ic_auth_eye_visible else R.drawable.ic_auth_eye_hidden
        )
        editText.setSelection(selection)
    }

    layout.endIconMode = TextInputLayout.END_ICON_CUSTOM
    layout.setEndIconOnClickListener {
        isVisible = !isVisible
        applyState()
    }

    applyState()
}
