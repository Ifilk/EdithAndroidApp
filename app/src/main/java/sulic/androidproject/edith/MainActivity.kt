package sulic.androidproject.edith

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.ResolveInfo
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionService
import android.speech.RecognizerIntent
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
    private val TAG = MainActivity::class.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val MIN_TIME_STEP = 1000L
        const val AUDIO_SAMPLE_INTERVAL = 1000L
        const val AUTO_INTERRUPT_TOLERANCE = 10
        const val AUTO_INTERRUPT_MIN_VOLUME = 5000
        var TextToSpeech = true
        var OriginTextToSpeechAvailable = false
        var SERVER_IP = "http://60.205.236.106:9002"
        var AvailableSystemSpeechRecognizer: SpeechRecognizer? = null
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

        requestMultiplePermissions.launch(arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE))

        //华为手机返回true, 但服务不可用
        OriginTextToSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(this)

        if(OriginTextToSpeechAvailable){
            val serviceComponent: String = Settings.Secure.getString(contentResolver,"voice_recognition_service")
            val component = ComponentName.unflattenFromString(serviceComponent)
            var currentRecognitionCmp: ComponentName? = null
            val list: List<ResolveInfo> =
                this.packageManager.queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), MATCH_ALL)
            if (list.isNotEmpty()) {
                for (info in list) {
                    // 检测拿到的是否是华为的假服务
                    Log.d(
                        TAG, "\t" + info.loadLabel(this.packageManager) + ": "
                                + info.serviceInfo.packageName + "/" + info.serviceInfo.name
                    )
                    if(info.serviceInfo.name ==
                        "com.huawei.vassistant.voiceui.service.FakeRecognitionService"){
                        if(list.size == 1){
                            OriginTextToSpeechAvailable.not()
                        }
                        continue
                    }
                    if (component != null) {
                        if (info.serviceInfo.packageName == component.packageName) {
                            OriginTextToSpeechAvailable = true
                            break
                        } else {
                            currentRecognitionCmp = ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "No recognition services installed")
                OriginTextToSpeechAvailable = false
            }
            AvailableSystemSpeechRecognizer = if (OriginTextToSpeechAvailable)
                SpeechRecognizer.createSpeechRecognizer(this)
            else if (currentRecognitionCmp != null)
                SpeechRecognizer.createSpeechRecognizer(this, currentRecognitionCmp)
            else null
            val mRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            mRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
//        AvailableSystemSpeechRecognizer.setRecognitionListener(STTRecognitionListener())
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