package sulic.androidproject.edith

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import sulic.androidproject.edith.databinding.ActivityMainBinding
import sulic.androidproject.edith.ui.component.ServerSettingsDialog.OnServerAddressSetListener
import sulic.androidproject.edith.ui.component.ServerSettingsDialog.show


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnStartRecord: Button
    private lateinit var recorder: MediaRecorder
    private lateinit var audioOutputPath: String
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mTextToSpeech: TextToSpeech
    private lateinit var startSTTButton: Button
    private lateinit var statusAnimation: ImageView

    companion object {
        const val MIN_TIME_STEP = 1000L
        const val AUDIO_SAMPLE_INTERVAL = 1000L
        const val AUTO_INTERRUPT_TOLERANCE = 10
        const val AUTO_INTERRUPT_MIN_VOLUME = 5000
        var TextToSpeech = true
        var SERVER_IP = "http://60.205.236.106:9002"
        lateinit var ACTIVITY: AppCompatActivity
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("requestMultiplePermissions", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        ACTIVITY = this

        initVar()

        requestMultiplePermissions.launch(arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE))


        var thread:Thread? = null
//        var stopRecorderFunction = {
//            btnStartRecord.text = "Start audio record"
//            with(recorder) {
//                stop()
//                release()
//            }
//            val tempFile = File(getExternalFilesDir(null)!!.absolutePath + "/output.3gp")
//            if(tempFile.exists()){
//                mMediaPlayer = MediaPlayer()
//                mMediaPlayer.setDataSource(tempFile.absolutePath)
//                mMediaPlayer.prepare()
//                mMediaPlayer.start()
//            }
//
//            Thread {
//                Log.d(TAG,"Start upload audio file")
//                upload("http://192.168.31.92:8080/contact", "output.3gp", null, tempFile.readBytes())?.let {
//                    Log.d(TAG,
//                        it
//                    )
//                    mTextToSpeech.speak(it as CharSequence, TextToSpeech.QUEUE_FLUSH, null, "0")
//                }
//            }.start()
//        }
//        var recordingFunction = {
//            btnStartRecord.text = "Recording"
//            recorder = MediaRecorder(this).apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setOutputFile(audioOutputPath)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                try {
//                    prepare()
//                } catch (e: IOException) {
//                    Log.e(TAG, "prepare() failed")
//                }
//                start()
//            }
//
//            if(thread == null){
//                thread = Thread {
//                    try{
//                        var amountOfSample = 0
//                        var isHinted = false
//                        Thread.sleep(MIN_TIME_STEP)
//                        while (true) {
//                            amountOfSample++
//                            val volume = recorder.maxAmplitude
//                            Log.d(TAG, "volume: $volume")
////                            if (volume < AUTO_INTERRUPT_MIN_VOLUME && amountOfSample < AUTO_INTERRUPT_TOLERANCE){
////                                if(!isHinted){
////                                    runOnUiThread{
////                                        showMsg("你的声音太小了,请大声点")
////                                    }
////                                    isHinted = true
////                                }
////                                continue
////                            }
////                            if(volume < AUTO_INTERRUPT_MIN_VOLUME){
////                                btnStartRecordState
////                                runOnUiThread{
////                                    stopRecorderFunction()
////                                }
////                                return@Thread
////                            }
//                            Thread.sleep(AUDIO_SAMPLE_INTERVAL)
//                        }
//                    } catch (e: Exception){
//                        Log.d(TAG, "onException: ${e.message}")
//                    }
//                }
//                thread!!.start()
//            } else {
//                thread!!.interrupt()
//                thread = null
//            }
//        }

//        btnStartRecord.setOnClickListener {
//            MediaPlayer.create(this, R.raw.button_click).start()
//            btnStartRecordState.around(recordingFunction, stopRecorderFunction)
//
////            if(!SpeechRecognizer.isRecognitionAvailable(this)){
////                showMsg("你的设备不原生支持文字转语言")
////                return@setOnClickListener
////            }
////
////            val serviceComponent: String = Settings.Secure.getString(
////                this.contentResolver,
////                "voice_recognition_service"
////            )
////            // 当前系统内置语音识别服务
////            val component = ComponentName.unflattenFromString(serviceComponent)
////            // 内置语音识别服务是否可用
////            var isRecognizerServiceValid = false
////            var currentRecognitionCmp: ComponentName? = null
////            // 查找得到的 "可用的" 语音识别服务
////            val list: List<ResolveInfo> =
////                this.packageManager.queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), MATCH_ALL)
////
////            if (list.isNotEmpty()) {
////                for (info in list) {
////                    Log.d(
////                        TAG, "\t" + info.loadLabel(this.packageManager) + ": "
////                                + info.serviceInfo.packageName + "/" + info.serviceInfo.name
////                    )
////                    // 这里拿系统使用的语音识别服务和内置的语音识别比较，如果相同，OK我们直接直接使用
////                    // 如果相同就可以直接使用mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);来创建实例，因为内置的可以使用
////                    if (component != null) {
////                        if (info.serviceInfo.packageName == component.packageName) {
////                            isRecognizerServiceValid = true
////                            break
////                        } else {
////                            // 如果服务不同，说明 内置服务 和 系统使用 不是同一个，那么我们需要使用系统使用的
////                            // 因为内置的系统不用，我们用了也没有用
////                            currentRecognitionCmp = ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)
////                        }
////                    }
////                }
////            } else {
////                // 这里既是查不到可用的语音识别服务，可以歇菜了
////                Log.d(TAG, "No recognition services installed")
////                isRecognizerServiceValid = false
////            }
////
////            // 当前系统内置语音识别服务可用
////            speechRecognizer = if (isRecognizerServiceValid) {
////                SpeechRecognizer.createSpeechRecognizer(this);
////            } else {
////                // 内置不可用，需要我们使用查找到的可用的
////                SpeechRecognizer.createSpeechRecognizer(this, currentRecognitionCmp);
////            }
////            val mRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
////            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
////            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
////            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
//////            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
////            speechRecognizer.setRecognitionListener(STTRecognitionListener())
////
////            btnStartRecordState.around({
////                speechRecognizer.startListening(mRecognitionIntent)
////                btnStartRecord.text = "Stop"
////            }){
////                speechRecognizer.stopListening()
////                speechRecognizer.cancel()
////                btnStartRecord.text = "Start"
////            }
//        }
//
//        startSTTButton.setOnClickListener {
//            setStatusAnimation(AnimationType.RECORDING)
//        }

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
    }

    override fun onStart() {
        super.onStart()
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu[0].setOnMenuItemClickListener {
            show(this, object : OnServerAddressSetListener {
                override fun onServerAddressSet(ipAddress: String?) {
                    SERVER_IP = ipAddress!!
                }

                override fun onDefaultTTsSet(b: Boolean?) {
                    TextToSpeech = b!!
                }
            })
            true
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun initVar(){
//        startSTTButton = findViewById(R.id.startSTTButton)
//        btnStartRecord = findViewById(R.id.startSTTButton)
        audioOutputPath = getExternalFilesDir(null)!!.absolutePath + "/output.3gp"
//        sttViewer = findViewById(R.id.STTView)
        mTextToSpeech = TextToSpeech(this, TTSListener())
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        Log.d("audioOutputPath", audioOutputPath)
        mMediaPlayer = MediaPlayer()
    }


    private fun showMsg(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun isOpenBluetooth(): Boolean {
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return false
        return adapter.isEnabled
    }

    private fun hasPermission(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

}

class TTSListener : OnInitListener {
    override fun onInit(status: Int) {
        // TODO Auto-generated method stub
        if (status == TextToSpeech.SUCCESS) {
//                int supported = mSpeech.setLanguage(Locale.US);
//                if ((supported != TextToSpeech.LANG_AVAILABLE) && (supported != TextToSpeech.LANG_COUNTRY_AVAILABLE)) {
//                    Toast.makeText(MainActivity.this, "不支持当前语言！", Toast.LENGTH_SHORT).show();
//                    Log.i(TAG, "onInit: 支持当前选择语言");
//                }else{
//
//                }
            Log.i("TTSListener", "onInit: TTS引擎初始化成功")
        } else {
            Log.i("TTSListener", "onInit: TTS引擎初始化失败")
        }
    }
}