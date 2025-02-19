package com.muchen.tweetstormmaker.androidui.view.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.muchen.tweetstormandroid.R
import com.muchen.tweetstormmaker.androidui.view.MainActivity

class LogoutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
            .setMessage(R.string.dialog_message_logout_dialog)
            .setPositiveButton(getString(R.string.btn_label_dialog_yes_button)) { _, _ ->
                val mainActivity: MainActivity = (requireActivity() as MainActivity)
                mainActivity.openCloseNavDrawer(false)
                mainActivity.twitterApiViewModel.logout()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.btn_label_dialog_no_button)) { _, _ ->
                val mainActivity: MainActivity = (requireActivity() as MainActivity)
                mainActivity.openCloseNavDrawer(false)
                findNavController().popBackStack()
            }
            .create()
    }
}