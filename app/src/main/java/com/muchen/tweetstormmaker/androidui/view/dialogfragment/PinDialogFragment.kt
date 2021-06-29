package com.muchen.tweetstormmaker.androidui.view.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.muchen.tweetstormmaker.R
import com.muchen.tweetstormmaker.androidui.view.MainActivity
import com.muchen.tweetstormmaker.databinding.FragmentDialogPinBinding

class PinDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentDialogPinBinding.inflate(layoutInflater)
        return AlertDialog.Builder(context).setTitle(getString(R.string.dialog_title_pin_dialog))
                .setView(binding.root)
                .setPositiveButton(getString(R.string.btn_label_dialog_positive_button)) { _, _ ->
                    val pin: String = binding.pinDialogEditText.text.toString()
                    (requireActivity() as MainActivity).twitterApiViewModel.finishLogin(pin)
                    findNavController().popBackStack()
                }
                .setNegativeButton(getString(R.string.btn_label_dialog_negative_button)) { _, _ ->
                    findNavController().popBackStack()
                }
                .create()
    }
}