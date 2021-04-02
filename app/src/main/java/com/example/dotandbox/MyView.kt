package com.example.dotandbox

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.lang.IllegalArgumentException

class MyView(context: Context) : View(context) {

    private val lineStrokeWidth = 20f
    private val circleSize = 15f

    private val horizonSize = 4
    private val verticalSize = 6

    private val red =  ContextCompat.getColor(context, R.color.red)
    private val blue =   ContextCompat.getColor(context, R.color.blue)
    private val black =  ContextCompat.getColor(context, R.color.black)

    private val lightRed =  ContextCompat.getColor(context, R.color.lightRed)
    private val lightBlue =   ContextCompat.getColor(context, R.color.lightBlue)

    // ドットタップ時の座標ズレの猶予
    private val margin = 50

    private var pathList = arrayListOf<PathData>()

    private var distance = 0

    // 1のときは始点,2のときは終点
    private var order = 1

    private var blueScore = 0
    private var redScore = 0

    //次の色
    private var nextColor = red

    //PathData作成用のデータ一時保管用変数
    private var startX = 0
    private var startY = 0
    private var endX = 0
    private var endY = 0

    // ステータスバー+タイトルバーの幅
    private var upSize = 0

    private var scoreDots = arrayListOf<Face>()
    private var scoreDotsHistory = arrayListOf<Face>()

    //すでにMainActivity#onWindowFocusChangedにてこのViewが正しく作成されたかどうか
    var isMade : Boolean = false

    init {
        pathList.clear()
    }

    // これをMainActivityから呼んでもらって大きさが正式に決まったことを教えてもらう
    // サイズの用語はと取得方法は以下のサイトで
    // https://techbooster.org/android/hacks/16066/
    fun onMade(displaySize:Int) {
        upSize = displaySize - this.height
        isMade = true
        distance = this.width / (horizonSize + 1)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if(!isMade) {
            return
        }
        // ここからは正しくScreenのサイズが取得された後にしか実行されない

        //画面の初期化
        //canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        this.setBackgroundColor(Color.WHITE)

        //Paintを作る
        var paint: Paint = Paint()
        paint.isAntiAlias = true

        //塗る
        for(i in scoreDotsHistory) {
            paint.color = makeColorLight(i.color)
            canvas.drawRect((i.point.x * distance).toFloat(), (i.point.y * distance).toFloat(), (i.point.x + 1) * distance.toFloat(),(i.point.y + 1) * distance.toFloat(),paint)
        }

        //線を書く
        for (l in pathList) {
            paint.color = l.color
            paint.strokeWidth = lineStrokeWidth

            canvas.drawLine((l.startXIndex * distance).toFloat(), (l.startYIndex * distance).toFloat(), (l.endXIndex * distance).toFloat(), (l.endYIndex * distance).toFloat(),paint)
        }

        //ドットを描く(先に線を書くことによって上から点をかけばイイ感じの見た目になる)
        paint.color = black
        for (i in 1..horizonSize) {
            for (j in 1..verticalSize) {
                canvas.drawCircle((distance * i).toFloat(), (distance * j).toFloat(), circleSize, paint)
            }
        }


    }

    fun onClicked(x:Float,y:Float) {
        val p : Point = convertCoordinate2Index(x,y)
        // タップ位置が許容範囲外だった場合そこで処理を終了
        if(p.x == -1) {
            return
        }

        if(order == 1) {
            startX = p.x
            startY = p.y
            if(pathList.contains(PathData(startX,startY,startX-1,startY,red))
                && pathList.contains(PathData(startX,startY,startX+1,startY,red))
                &&pathList.contains(PathData(startX,startY,startX,startY-1,red))
                &&pathList.contains(PathData(startX,startY,startX,startY+1,red))){
                Toast.makeText(context,"そこは選択できません",Toast.LENGTH_SHORT).show()
                return
            }
            order = 2
        }else if(order == 2) {
            endX = p.x
            endY = p.y
            if((!((startX-endX == 1 || startX-endX == -1  || startX == endX) && (startY-endY == 1 || startY-endY == -1 || startY == endY)) || (startX-endX)*(startY-endY) != 0 || (startX == endX && startY == endY)) || pathList.contains(
                    PathData(startX,startY,endX,endY,red)
                )) {
                Toast.makeText(context,"そこは選択できません",Toast.LENGTH_SHORT).show()
                return
            }
            order = 1
            pathList.add(PathData(startX,startY,endX,endY,nextColor))
            checkScore()
            if(scoreDots.count() != 0) {
                Toast.makeText(context,"得点!",Toast.LENGTH_SHORT).show()
                if(nextColor == red) {
                    redScore += scoreDots.count()
                }else if(nextColor == blue) {
                    blueScore += scoreDots.count()
                }
                for(i in scoreDots) {
                    scoreDotsHistory.add(i)
                }
            }else{
                changeColor()
            }
            invalidate()
            if(scoreDotsHistory.count() == (horizonSize - 1) * (verticalSize - 1)) {
                var result : String = ""
                result = if(redScore > blueScore) {
                    "赤の勝ち!"
                }else if(blueScore > redScore) {
                    "青の勝ち!"
                }else{
                    "引き分け!"
                }
                AlertDialog.Builder(context) // FragmentではActivityを取得して生成
                    .setTitle("ゲーム終了")
                    .setMessage("赤:" + redScore.toString() + ",青" + blueScore.toString() + "で" + result)
                    .setPositiveButton("終了") { _, _ ->
                        //Yesが押された時の挙動
                    }
                    .setNegativeButton("もう一度") { dialog, which ->
                        //Noが押された時の挙動
                        reset()
                    }
                    .show()
            }
        }
    }

    private fun convertCoordinate2Index(x:Float, y:Float) : Point{
        // y軸に関してはタイトルバーとかのせいで正しく座標が取得できていないためupSizeを引くことで修正
        val xInt = x.toInt()
        val yInt = (y - upSize).toInt()

        val xIntM = xInt % distance
        val yIntM = yInt % distance

        if((xIntM < margin || xIntM > (distance - margin)) && (yIntM < margin || yIntM > (distance - margin))) {

            var pX : Int= 0
            var pY : Int= 0

            if(xIntM < margin) {
                pX = (xInt - xIntM) / distance
            }else if(xIntM > (distance - margin)) {
                pX = (xInt + (distance - xIntM)) / distance
            }

            if(yIntM < margin) {
                pY = (yInt - yIntM) / distance
            }else if(yIntM > (distance - margin)) {
                pY = (yInt + (distance - yIntM)) / distance
            }

            return Point(pX,pY)
        } else{
            return Point(-1,-1)
        }
    }

    //色をもう一方に変える
    private fun changeColor() {
        if(nextColor == red) {
            nextColor = blue
        }else if(nextColor == blue) {
            nextColor = red
        }
    }

    private fun makeColorLight(color:Int): Int {
        if(color == red) {
            return lightRed
        }else if(color == blue) {
            return lightBlue
        }else{
            throw IllegalArgumentException("Strange color.")
        }
    }

    private fun checkScore() {
        scoreDots.clear()
        for (i in 1..horizonSize) {
            for (j in 1..verticalSize) {
                if(pathList.contains(PathData(i,j,i+1,j,red))
                    && pathList.contains(PathData(i,j,i,j+1,red))
                    && pathList.contains(PathData(i,j+1,i+1,j+1,red))
                    && pathList.contains(PathData(i+1,j,i+1,j+1,red))
                    && !scoreDotsHistory.contains(Face(Point(i,j),red)))
                {
                    scoreDots.add(Face(Point(i,j),nextColor))
                }
            }
        }
    }

    private fun reset() {
        pathList.clear()
        order = 1
        redScore = 0
        blueScore = 0
        scoreDots.clear()
        scoreDotsHistory.clear()
        invalidate()
    }

    @Deprecated("Use checkScore() instead.")
    fun checkScorePrevious(newPath:PathData){
        scoreDots.clear()
        if(newPath.startXIndex == newPath.endXIndex) {
            //x軸が変わらない=縦の線
            val hasLeft : Boolean =
                pathList.contains(PathData(newPath.startXIndex-1,newPath.startYIndex,newPath.endXIndex-1,newPath.endYIndex,red))
                        && pathList.contains(PathData(newPath.startXIndex-1,newPath.startYIndex,newPath.endXIndex,newPath.endYIndex-1,red))
                        && pathList.contains(PathData(newPath.startXIndex-1,newPath.startYIndex+1,newPath.endXIndex,newPath.endYIndex,red))

            val hasRight:Boolean =
                pathList.contains(PathData(newPath.startXIndex+1,newPath.startYIndex,newPath.endXIndex+1,newPath.endYIndex,red))
                        && pathList.contains(PathData(newPath.startXIndex,newPath.startYIndex,newPath.endXIndex+1,newPath.endYIndex-1,red))
                        && pathList.contains(PathData(newPath.startXIndex+1,newPath.startYIndex+1,newPath.endXIndex,newPath.endYIndex,red))

            if(hasLeft) {
                if(newPath.startYIndex > newPath.endYIndex) {
                    scoreDots.add(Face(Point(newPath.endXIndex-1,newPath.endYIndex),nextColor))
                }else{
                    scoreDots.add(Face(Point(newPath.startXIndex-1,newPath.startYIndex),nextColor))
                }
            }
            if(hasRight) {
                if(newPath.startYIndex > newPath.endYIndex) {
                    scoreDots.add(Face(Point(newPath.endXIndex,newPath.endYIndex),nextColor))
                }else{
                    scoreDots.add(Face(Point(newPath.startXIndex,newPath.startYIndex),nextColor))
                }
            }
        }else if(newPath.startYIndex == newPath.endYIndex) {
            //y軸が変わらない=横の線

            // ここの3行目?がおかしい
            val hasAbove : Boolean =
                pathList.contains(PathData(newPath.startXIndex,newPath.startYIndex-1,newPath.endXIndex,newPath.endYIndex-1,red))
                        && pathList.contains(PathData(newPath.startXIndex,newPath.startYIndex,newPath.endXIndex-1,newPath.endYIndex-1,red))
                        && pathList.contains(PathData(newPath.startXIndex+1,newPath.startYIndex-1,newPath.endXIndex,newPath.endYIndex,red))


            val hasBelow : Boolean =
                pathList.contains(PathData(newPath.startXIndex,newPath.startYIndex+1,newPath.endXIndex,newPath.endYIndex+1,red))
                        && pathList.contains(PathData(newPath.startXIndex,newPath.startYIndex,newPath.endXIndex-1,newPath.endYIndex+1,red))
                        && pathList.contains(PathData(newPath.startXIndex+1,newPath.startYIndex+1,newPath.endXIndex,newPath.endYIndex,red))

            if(hasAbove) {
                if(newPath.startXIndex > newPath.endXIndex) {
                    scoreDots.add(Face(Point(newPath.endXIndex,newPath.endYIndex-1),nextColor))
                }else{
                    scoreDots.add(Face(Point(newPath.startXIndex,newPath.startYIndex - 1),nextColor))
                }
            }

            if(hasBelow) {
                if(newPath.startXIndex > newPath.endXIndex) {
                    scoreDots.add(Face(Point(newPath.endXIndex,newPath.endYIndex),nextColor))
                }else{
                    scoreDots.add(Face(Point(newPath.startXIndex,newPath.startYIndex),nextColor))
                }
            }
        }
    }

}