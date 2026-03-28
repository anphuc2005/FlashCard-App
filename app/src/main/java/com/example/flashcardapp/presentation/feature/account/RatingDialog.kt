package com.example.flashcardapp.presentation.feature.account

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogRatingBinding

class RatingDialog : DialogFragment() {

    interface Listener {
        fun onSubmit(rating: Int, comment: String)
    }

    var listener: Listener? = null

    private var _binding: DialogRatingBinding? = null
    private val binding get() = _binding!!

    private var rating: Int = 4
    private var comment: String = ""

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRatingBinding.inflate(LayoutInflater.from(requireContext()))

        rating = arguments?.getInt(ARG_RATING) ?: rating
        comment = arguments?.getString(ARG_COMMENT) ?: comment

        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { updateRating(index + 1) }
        }

        binding.etComment.setText(comment)

        binding.btnSubmit.setOnClickListener {
            listener?.onSubmit(rating, binding.etComment.text?.toString().orEmpty())
            dismiss()
        }

        updateRating(rating)
    }

    private fun updateRating(value: Int) {
        rating = value
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, imageView ->
            val res = if (index < value) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            imageView.setImageResource(res)
            imageView.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val ARG_RATING = "arg_rating"
        private const val ARG_COMMENT = "arg_comment"

        fun newInstance(rating: Int, comment: String): RatingDialog = RatingDialog().apply {
            arguments = Bundle().apply {
                putInt(ARG_RATING, rating)
                putString(ARG_COMMENT, comment)
            }
        }
    }
}
