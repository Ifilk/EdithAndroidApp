package sulic.androidproject.edith.service.impl

import android.content.Intent
import android.speech.RecognitionListener
import sulic.androidproject.edith.service.SpeechRecognitionService

class DefaultRemoteSpeechRecognitionService: SpeechRecognitionService {
    private var listener: RecognitionListener? = null

    override fun setRecognitionListener(listener: RecognitionListener) {
        this.listener = listener
    }

    override fun startListening(intent: Intent) {
        TODO("Not yet implemented")
    }

    override fun stopListening() {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        TODO("Not yet implemented")
    }
}