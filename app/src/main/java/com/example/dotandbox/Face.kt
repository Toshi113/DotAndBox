package com.example.dotandbox

import android.graphics.Point

data class Face(val point : Point,val color: Int) {
    override fun equals(other: Any?): Boolean {
        try {
            var face: Face = other as Face
            //colorをequals判定に含めないことでarrayListのcontainsで色が違っていても他が同じならtrueを返すようにする。(ホントは良くないかも)
            if (this.point == face.point) {
                return true
            }
        }catch (e: Exception) {
            return false
        }
        return false
    }
}