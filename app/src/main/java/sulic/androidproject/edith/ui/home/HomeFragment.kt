package sulic.androidproject.edith.ui.home

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import org.json.JSONObject
import sulic.androidproject.edith.MainActivity
import sulic.androidproject.edith.R
import sulic.androidproject.edith.TTSListener
import sulic.androidproject.edith.common.executeAround
import sulic.androidproject.edith.databinding.FragmentHomeBinding
import sulic.androidproject.edith.dto.CompletionDto.Companion.toDefaultCompletionDto
import sulic.androidproject.edith.service.LLMRemoteService
import sulic.androidproject.edith.service.Properties
import sulic.androidproject.edithandroidapp2.adapter.Msg
import sulic.androidproject.edithandroidapp2.adapter.MsgAdapter
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs


@AndroidEntryPoint
class HomeFragment @Inject constructor(): Fragment() {
    @Inject
    lateinit var llmRemoteService: LLMRemoteService
    @Inject
    lateinit var properties: Properties

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
    private lateinit var navController: NavController
    private lateinit var detector: GestureDetector


    private val defaultCallExceptionHandler = {call: Call, e: IOException ->
        displayMsg(e.message, Msg.TYPE_RECEIVED)
    }

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
//        val textView: TextView
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        binding.msgRecyclerView.layoutManager = LinearLayoutManager(root.context)
        binding.msgRecyclerView.adapter = msgAdapter
        audioOutputPath = root.context.getExternalFilesDir(null)!!.absolutePath + "/output.3gp"
        mMediaPlayer = MediaPlayer()
        mTextToSpeech = TextToSpeech(root.context, TTSListener())
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
        detector = GestureDetector(activity, object : SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("HomeFragment", "$e1 $e2")
                // e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
                //判断竖直方向移动的大小
                if (abs((e1!!.rawY - e2.rawY).toDouble()) > 150) {
//                    Toast.makeText(context, "动作不合法", Toast.LENGTH_SHORT).show()
                    return true
                }
                if ((e1.rawX - e2.rawX) > 40) { // 表示 向右滑动表示下一页
                    //显示下一页
                    navController.navigate(R.id.nav_slideshow, null,
                        NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_right)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_left)
                            .build())
                    return true
                }
                if ((e2.rawX - e1.rawX) > 40) {  //向左滑动 表示 上一页
                    //显示上一页
                    navController.navigateUp()
                    return true
                }
                if (abs(velocityX.toDouble()) < 150) {
                    //Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
                    return true
                }

                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        binding.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                llmRemoteService.completion(arrayOf(binding.editText.text.toString()).toDefaultCompletionDto())
                    .executeAround(defaultCallExceptionHandler){ call, response ->
                        val arr = JSONObject(response.body?.string()).getJSONArray("choices")
                        for (i in 0 until arr.length()) {
                            val msg = (arr.get(i) as JSONObject).getJSONObject("message").getString("content")
                            displayMsg(msg, Msg.TYPE_RECEIVED)
//                            if(MainActivity.TextToSpeech)
//                                mTextToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "0")
                        }
                        displayMsgFromReceive(response.body?.string())
                    }
                return@setOnEditorActionListener true
            }
            false
        }
        MainActivity.ACTIVITY.registerMyOnTouchListener(object : MainActivity.OnTouchListener {
            override fun onTouch(ev: MotionEvent?): Boolean {
                return ev?.let { detector.onTouchEvent(it) } ?: true
            }
        })

//        binding.sendButton.setOnClickListener {
//            val s = binding.editText.text.toString()
//            displayMsg(s, Msg.TYPE_SEND)
//            binding.editText.text.clear()
//            val imm = MainActivity.ACTIVITY.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
//            llmRemoteService.completion(arrayOf(s).toDefaultCompletionDto())
//                .executeAround(defaultCallExceptionHandler){ call, response ->
//                    val arr = JSONObject(response.body?.string()).getJSONArray("choices")
//                    for (i in 0 until arr.length()) {
//                        val msg = (arr.get(i) as JSONObject).getJSONObject("message").getString("content")
//                        displayMsg(msg, Msg.TYPE_RECEIVED)
//                        if(MainActivity.TextToSpeech)
//                            mTextToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "0")
//                    }
//                    displayMsgFromReceive(response.body?.string())
//                    enableSendButton()
//                }
//            disableSendButton()
//        }
//        binding.fab.setOnTouchListener { v, event ->
//            when(event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    MediaPlayer.create(root.context, R.raw.button_click).start()
//                    v.startAnimation(AnimationUtils.loadAnimation(root.context, R.anim.recording_animation))
//                    recorder = MediaRecorder(root.context).apply {
//                        setAudioSource(MediaRecorder.AudioSource.MIC)
//                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                        setOutputFile(audioOutputPath)
//                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                        try {
//                            prepare()
//                            start()
//                            recordingTime = System.currentTimeMillis()
//                            isPrepared = true
//                        } catch (e: IOException) {
//                            showMsg(root.context, getString(R.string.microphone_busy))
//                            isPrepared = false
//                        }
//                    }
//
//                }
//                MotionEvent.ACTION_UP -> {
//                    if (!isPrepared &&
//                        System.currentTimeMillis() - recordingTime < 1000){
//                        showMsg(root.context, getString(R.string.overshort_audio))
//                        return@setOnTouchListener true
//                    }
//                    MediaPlayer.create(root.context, R.raw.button_click).start()
//                    binding.fab.performClick()
////                    binding.fab.setImageResource()
//                    with(recorder) {
//                        stop()
//                        release()
//                    }
//                    val tempFile = File(audioOutputPath)
//                    if(tempFile.exists()){
//                        mMediaPlayer = MediaPlayer()
//                        mMediaPlayer.setDataSource(tempFile.absolutePath)
//                        mMediaPlayer.prepare()
//                        mMediaPlayer.start()
//                        Thread {
//                            val result0 = upload("${properties.SERVER_IP}/stt", getString(R.string.audio_output_filename), tempFile)
//                            MainActivity.ACTIVITY.runOnUiThread {
//                                val s = result0.replace("\"", "")
//                                displayMsg(s, Msg.TYPE_SEND)
//                                Thread {
//                                    val result1 = post("${properties.SERVER_IP}/contact",
//                                        mapOf("role" to "user", "content" to s).toJSONString())
//                                    MainActivity.ACTIVITY.runOnUiThread {
//                                        displayMsg(result1.replace("\"", ""), Msg.TYPE_RECEIVED)
//                                        if(properties.TextToSpeech)mTextToSpeech.speak(result1.replace("\"", ""), TextToSpeech.QUEUE_FLUSH, null, "0")
//                                        v.clearAnimation()
//                                        binding.fab.isEnabled = true
//                                    }
//                                }.start()
//                            }
//                        }.start()
//                        v.startAnimation(AnimationUtils.loadAnimation(root.context, R.anim.computing_animation))
//                        binding.fab.isEnabled = false
//                    } else showMsg(root.context, getString(R.string.audio_file_not_found))
//                }
//            }
//            true
//        }

        return root
    }

    private fun displayMsgFromReceive(msg: String?) = displayMsg(msg, Msg.TYPE_RECEIVED)

    private fun displayMsg(msg: String?, type: Int){
        MainActivity.ACTIVITY.runOnUiThread{
            msgContext.add(Msg(msg, type))
            msgAdapter.notifyItemInserted(msgContext.size - 1)
            binding.msgRecyclerView.scrollToPosition(msgContext.size - 1)
        }
    }

//    private fun enableSendButton(){
//        binding.sendButton.isEnabled = false
//    }
//
//    private fun disableSendButton(){
//        binding.sendButton.isEnabled = true
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun send(s: String){
        Thread {
//            val result = post("${Properties.SERVER_IP}/contact",
//                mapOf("role" to "user", "content" to s).toJSONString())
                MainActivity.ACTIVITY.runOnUiThread {
//                    val arr = JSONObject(result).getJSONArray("choices")
//                    for (i in 0 until arr.length()) {
//                        val msg = (arr.get(i) as JSONObject).getJSONObject("message").getString("content")
//                        displayMsg(msg, Msg.TYPE_RECEIVED)
//                        if(MainActivity.TextToSpeech)
//                            mTextToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "0")
//                    }
//                    enableSendButton()

                }
        }.start()
    }
}