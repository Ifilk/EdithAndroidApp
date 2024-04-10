package sulic.androidproject.edith.ui.component

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StreamTextView(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    private val handler = Handler(Looper.getMainLooper())
    private val stringBuffer = StringBuffer()
    private val stringBuilder = StringBuilder()

    companion object{
        const val DISPLAY_DELAY = 50L
    }

    fun display(m: String?){
        stringBuffer.setLength(0)
        stringBuilder.setLength(0)
        push(m)
    }

    fun push(m: String?){
        stringBuffer.append(m)
        if(stringBuffer.isNotEmpty()) update()
    }

    private fun update(){
//        while(stringBuffer.isNotEmpty()){
//            stringBuilder.append(stringBuffer[0])
//            stringBuffer.deleteCharAt(0)
//            text = stringBuilder.toString()
////            Thread.sleep(DISPLAY_DELAY)
//        }
        if (stringBuffer.isEmpty()) return
        stringBuilder.append(stringBuffer[0])
        stringBuffer.deleteCharAt(0)
        text = stringBuilder.toString()
        handler.postDelayed({ update() }, DISPLAY_DELAY)
    }
}
