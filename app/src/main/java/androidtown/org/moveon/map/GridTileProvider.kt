package androidtown.org.moveon.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.io.ByteArrayOutputStream

class GridTileProvider : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val tileSize = 256 // 타일 크기 (픽셀 단위)
        val bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // 격자 패턴 생성
        val cellSize = tileSize / 10f
        for (i in 0..10) {
            canvas.drawLine(0f, i * cellSize, tileSize.toFloat(), i * cellSize, paint)
            canvas.drawLine(i * cellSize, 0f, i * cellSize, tileSize.toFloat(), paint)
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return Tile(tileSize, tileSize, byteArray)
    }
}
