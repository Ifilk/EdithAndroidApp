package sulic.androidproject.edith.ui.service

import android.content.Intent
import android.speech.RecognitionListener

interface SpeechRecognitionService {
    fun setRecognitionListener(listener: RecognitionListener)

    fun startListening(intent: Intent)

    fun stopListening()

    fun cancel()
}