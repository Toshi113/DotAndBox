package com.example.dotandbox

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(){

    var horizonContentSize : Int = 0
    var verticalContentSize : Int = 0
    var myView : MyView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myView = MyView(this)
        setContentView(myView)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val display = windowManager.defaultDisplay
        val p = Point()
        display.getSize(p)
        myView?.onMade(p.y)
    }

    fun convertDp2Px(dp: Float, context: Context): Float {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return dp * metrics.density
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                //何もしないでござる
            }
            MotionEvent.ACTION_UP -> {
                myView?.onClicked(event!!.x,event!!.y)
            }
        }
        return super.onTouchEvent(event)
    }
}
