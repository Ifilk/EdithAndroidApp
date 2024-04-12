package sulic.androidproject.edith.ui.home

import android.annotation.SuppressLint
import android.content.Context.INPUT_METHOD_SERVICE
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject
import sulic.androidproject.edith.MainActivity
import sulic.androidproject.edith.R
import sulic.androidproject.edith.TTSListener
import sulic.androidproject.edith.common.post
import sulic.androidproject.edith.common.showMsg
import sulic.androidproject.edith.common.upload
import sulic.androidproject.edith.databinding.FragmentHomeBinding
import sulic.androidproject.edithandroidapp2.adapter.Msg
import sulic.androidproject.edithandroidapp2.adapter.MsgAdapter
import java.io.File
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val msgContext = ArrayList<Msg>()
    private val msgAdapter = MsgAdapter(msgContext)
    private lateinit var audioOutputPath: String
    private lateinit var recorder: MediaRecorder
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mTextToSpeech: TextToSpeech

    private var isPrepared = false
    private var recordingTime = -1L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
//
//        val textView: TextView
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        binding.msgRecyclerView.layoutManager = LinearLayoutManager(root.context)
        binding.msgRecyclerView.adapter = msgAdapter
        audioOutputPath = root.context.getExternalFilesDir(null)!!.absolutePath + "/output.3gp"
        mMediaPlayer = MediaPlayer()
        mTextToSpeech = TextToSpeech(root.context, TTSListener())

        binding.sendButton.setOnClickListener {
            val s = binding.editText.text.toString()
            displayMsg(s, Msg.TYPE_SEND)
            binding.editText.setText("")
            val imm = MainActivity.ACTIVITY.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)

            Thread {
//                val result1 = post("${MainActivity.SERVER_IP}/chat/completions",
//                    JSONObject(mapOf("role" to "user", "content" to s)).toString())
                val result1 = post("${MainActivity.SERVER_IP}/chat/completions",
                    """
                        {
                          "frequency_penalty": 1,
                          "max_tokens": 1000,
                          "messages": [
                            {
                              "content": "$s",
                              "raw": false,
                              "role": "user"
                            }
                          ],
                          "model": "rwkv",
                          "presence_penalty": 0,
                          "presystem": true,
                          "stream": false,
                          "temperature": 1,
                          "top_p": 0.3
                        }
                    """.trimIndent())
                MainActivity.ACTIVITY.runOnUiThread {
                    val m = result1.replace("\"", "")
                    displayMsg(m, Msg.TYPE_RECEIVED)
                    if(MainActivity.TextToSpeech)
                        mTextToSpeech.speak(m, TextToSpeech.QUEUE_FLUSH, null, "0")
                    binding.sendButton.isEnabled = true
                }
            }.start()

            binding.sendButton.isEnabled = false
        }
        binding.fab.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    MediaPlayer.create(root.context, R.raw.button_click).start()
                    v.startAnimation(AnimationUtils.loadAnimation(root.context, R.anim.recording_animation))
                    recorder = MediaRecorder(root.context).apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setOutputFile(audioOutputPath)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        try {
                            prepare()
                            start()
                            recordingTime = System.currentTimeMillis()
                            isPrepared = true
                        } catch (e: IOException) {
                            showMsg(root.context, getString(R.string.microphone_busy))
                            isPrepared = false
                        }
                    }

                }
                MotionEvent.ACTION_UP -> {
                    if (!isPrepared &&
                        System.currentTimeMillis() - recordingTime < 1000){
                        showMsg(root.context, getString(R.string.overshort_audio))
                        return@setOnTouchListener true
                    }
                    MediaPlayer.create(root.context, R.raw.button_click).start()
                    binding.fab.performClick()
//                    binding.fab.setImageResource()
                    with(recorder) {
                        stop()
                        release()
                    }
                    val tempFile = File(audioOutputPath)
                    if(tempFile.exists()){
                        mMediaPlayer = MediaPlayer()
                        mMediaPlayer.setDataSource(tempFile.absolutePath)
                        mMediaPlayer.prepare()
                        mMediaPlayer.start()
                        Thread {
                            val result0 = upload("${MainActivity.SERVER_IP}/stt", getString(R.string.audio_output_filename), tempFile)
                            MainActivity.ACTIVITY.runOnUiThread {
                                val s = result0.replace("\"", "")
                                displayMsg(s, Msg.TYPE_SEND)
                                Thread {
                                    val result1 = post("${MainActivity.SERVER_IP}/contact",
                                        JSONObject(mapOf("role" to "user", "content" to s)).toString())
                                    MainActivity.ACTIVITY.runOnUiThread {
                                        displayMsg(result1.replace("\"", ""), Msg.TYPE_RECEIVED)
                                        if(MainActivity.TextToSpeech)mTextToSpeech.speak(result1.replace("\"", ""), TextToSpeech.QUEUE_FLUSH, null, "0")
                                        v.clearAnimation()
                                        binding.fab.isEnabled = true
                                    }
                                }.start()
                            }
                        }.start()
                        v.startAnimation(AnimationUtils.loadAnimation(root.context, R.anim.computing_animation))
                        binding.fab.isEnabled = false
                    } else showMsg(root.context, getString(R.string.audio_file_not_found))
                }
            }
            true
        }

        return root
    }

    private fun displayMsg(msg: String, type: Int){
        msgContext.add(Msg(msg, type))
        msgAdapter.notifyItemInserted(msgContext.size - 1)
        binding.msgRecyclerView.scrollToPosition(msgContext.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}