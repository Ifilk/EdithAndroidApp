package sulic.androidproject.edith

//import androidx.viewpager2.widget.ViewPager2
//import sulic.androidproject.edith.ui.component.adapter.HomeFragmentAdapter
import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
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
import dagger.hilt.android.AndroidEntryPoint
import sulic.androidproject.edith.databinding.ActivityMainBinding
import sulic.androidproject.edith.service.Properties
import sulic.androidproject.edith.ui.component.ServerSettingsDialog
import sulic.androidproject.edith.ui.component.ServerSettingsDialog.OnServerAddressSetListener
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    @Inject
    lateinit var properties: Properties

    @Inject
    lateinit var serverSettingsDialog: ServerSettingsDialog

    private val TAG = MainActivity::class.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var ACTIVITY: MainActivity
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
        properties.OriginTextToSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(this)

        if(properties.OriginTextToSpeechAvailable){
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
                            properties.OriginTextToSpeechAvailable.not()
                        }
                        continue
                    }
                    if (component != null) {
                        if (info.serviceInfo.packageName == component.packageName) {
                            properties.OriginTextToSpeechAvailable = true
                            break
                        } else {
                            currentRecognitionCmp = ComponentName(info.serviceInfo.packageName, info.serviceInfo.name)
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "No recognition services installed")
                properties.OriginTextToSpeechAvailable = false
            }
            properties.AvailableSystemSpeechRecognizer = if (properties.OriginTextToSpeechAvailable)
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
                R.id.nav_home, R.id.nav_settings, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        findViewById<ViewPager2>(R.id.viewPager).adapter = HomeFragmentAdapter(this)


//        object : SimpleOnGestureListener() {
//            override fun onFling(
//                e1: MotionEvent?, e2: MotionEvent, velocityX: Float,
//                velocityY: Float
//            ): Boolean {
//                Log.d("HomeFragment", "$e1 $e2")
//                // e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
//                //判断竖直方向移动的大小
//                if (abs((e1!!.rawY - e2.rawY).toDouble()) > 100) {
//                    //Toast.makeText(getApplicationContext(), "动作不合法", 0).show();
//                    return true
//                }
//                if(supportFragmentManager.findFragmentById(R.id.app_bar_main) is HomeFragment){
//                    if ((e1.rawX - e2.rawX) > 200) { // 表示 向右滑动表示下一页
//                        //显示下一页
//                        Log.d("HomeFragment", "in left ")
//                        val intent = Intent(this@MainActivity, LOTMonitorActivity::class.java)
//                        startActivity(intent)
//                        ACTIVITY.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
//                        return true
//                    }
//                }
//                if (abs(velocityX.toDouble()) < 150) {
//                    //Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
//                    return true
//                }
//                if ((e2.rawX - e1.rawX) > 200) {  //向左滑动 表示 上一页
//                    //显示上一页
//
//                    return true
//                }
//                return super.onFling(e1, e2, velocityX, velocityY)
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu[0].setOnMenuItemClickListener {
            serverSettingsDialog.show(this, object : OnServerAddressSetListener {
                override fun onServerAddressSet(ipAddress: String?) {
                    properties.SERVER_IP = ipAddress!!
                }
                override fun onDefaultTTsSet(b: Boolean?) {
                    properties.TextToSpeech = b!!
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

    private val onTouchListeners = ArrayList<OnTouchListener>(10)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (listener in onTouchListeners) {
            listener.onTouch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun registerMyOnTouchListener(onTouchListener: OnTouchListener) {
        onTouchListeners.add(onTouchListener)
    }

    fun unregisterMyOnTouchListener(onTouchListener: OnTouchListener) {
        onTouchListeners.remove(onTouchListener)
    }

    interface OnTouchListener {
        fun onTouch(ev: MotionEvent?): Boolean
    }
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