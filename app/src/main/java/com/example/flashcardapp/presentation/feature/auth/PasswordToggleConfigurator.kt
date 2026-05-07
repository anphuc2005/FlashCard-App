package com.example.flashcardapp.presentation.feature.auth

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.example.flashcardapp.R
import com.google.android.material.textfield.TextInputLayout

object PasswordToggleConfigurator {

    fun setup(textInputLayout: TextInputLayout, editText: EditText) {
        textInputLayout.setEndIconDrawable(R.drawable.ic_auth_eye_hidden)
        editText.transformationMethod = PasswordTransformationMethod.getInstance()

        textInputLayout.setEndIconOnClickListener {
            togglePasswordVisibility(editText, textInputLayout)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, textInputLayout: TextInputLayout) {
        val isVisible = editText.transformationMethod is HideReturnsTransformationMethod

        if (isVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            textInputLayout.setEndIconDrawable(R.drawable.ic_auth_eye_hidden)
        } else {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            textInputLayout.setEndIconDrawable(R.drawable.ic_auth_eye_visible)
        }

        editText.setSelection(editText.text?.length ?: 0)
    }
}
