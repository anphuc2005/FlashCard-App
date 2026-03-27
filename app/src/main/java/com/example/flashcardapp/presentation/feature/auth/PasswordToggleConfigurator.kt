package com.example.flashcardapp.presentation.feature.auth

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.example.flashcardapp.R
import com.google.android.material.textfield.TextInputLayout

object PasswordToggleConfigurator {

    fun setup(textInputLayout: TextInputLayout, editText: EditText) {
        val toggleButton = textInputLayout.setEndIconOnClickListener { 
            togglePasswordVisibility(editText, textInputLayout)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, textInputLayout: TextInputLayout) {
        if (editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // Hide password
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            // Show password
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }
}

