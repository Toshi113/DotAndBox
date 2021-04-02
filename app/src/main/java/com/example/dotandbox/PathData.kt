package com.example.dotandbox

import android.graphics.Color

data class PathData(val startXIndex:Int,val startYIndex:Int,val endXIndex:Int,val endYIndex:Int,val color: Int) {
    override fun equals(other: Any?): Boolean {
        try {
            var pd: PathData = other as PathData
            //colorをequals判定に含めないことでarrayListのcontainsで色が違っていても他が同じならtrueを返すようにする。(ホントは良くないかも)
            if (this.startXIndex == pd.startXIndex && this.startYIndex == pd.startYIndex && this.endXIndex == pd.endXIndex && this.endYIndex == pd.endYIndex) {
                return true
            }
            //始まりと終わりが逆でも指定してるパスは同じだからtrueを返すようにする。
            if (this.startXIndex == pd.endXIndex && this.startYIndex == pd.endYIndex && this.endXIndex == pd.startXIndex && this.endYIndex == pd.startYIndex) {
                return true
            }
        }catch (e: Exception) {
            return false
        }
        return false
    }
}