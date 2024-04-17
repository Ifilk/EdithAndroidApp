package sulic.androidproject.edith.service

import android.speech.SpeechRecognizer
import javax.inject.Inject
import javax.inject.Singleton

class Properties @Inject constructor() {
    var TextToSpeech = true
    var OriginTextToSpeechAvailable = false
    var SERVER_IP = "http://60.205.236.106:9002"
    var AvailableSystemSpeechRecognizer: SpeechRecognizer? = null
}