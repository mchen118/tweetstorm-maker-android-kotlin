package com.muchen.tweetstormmaker.androidui.view.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muchen.tweetstormmaker.R

class RedirectDialogFragment : DialogFragment() {

    private val args: RedirectDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setMessage(getString(R.string.dialog_message_redirect_dialog) + args.authorizationUrl)
                .setPositiveButton(getString(R.string.btn_label_dialog_positive_button)) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(args.authorizationUrl))
                    findNavController().navigate(R.id.action_redirect_dialog_fragment_to_pin_dialog_fragment)
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.btn_label_dialog_negative_button)) { _, _ ->
                    findNavController().popBackStack()
                }
                .create()
    }
}