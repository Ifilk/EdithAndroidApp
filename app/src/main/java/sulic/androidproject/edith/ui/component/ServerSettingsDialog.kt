package sulic.androidproject.edith.ui.component

import android.app.AlertDialog
import android.content.Context
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import sulic.androidproject.edith.MainActivity


object ServerSettingsDialog {
    fun show(context: Context?, listener: OnServerAddressSetListener?) {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val ipAddressInput = EditText(context)
        ipAddressInput.setText(MainActivity.SERVER_IP)
        ipAddressInput.hint = "Enter server IP address"
        layout.addView(ipAddressInput)

        val ttsCheckBox = CheckBox(context)
        ttsCheckBox.text = "Enable TTS by default"
        layout.addView(ttsCheckBox)
        ttsCheckBox.isChecked = MainActivity.TextToSpeech

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Server Settings")
        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            val ipAddress = ipAddressInput.text.toString().trim { it <= ' ' }
            if (ipAddress.isNotEmpty()) {
                listener?.onServerAddressSet(ipAddress)
                listener?.onDefaultTTsSet(ttsCheckBox.isChecked)
            } else {
                Toast.makeText(context, "Please enter a valid IP address", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, i -> dialogInterface.dismiss() }

        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }

    interface OnServerAddressSetListener {
        fun onServerAddressSet(ipAddress: String?)
        fun onDefaultTTsSet(b: Boolean?)
    }
}
