package androidtown.org.moveon.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class GridManager(private val gridSize: Double) {
    private val visitedGrids = mutableMapOf<String, Polygon>()

    /**
     * 사용자의 현재 위치에 따라 방문한 격자를 색칠
     */
    fun markGridAsVisited(map: GoogleMap, userLocation: LatLng) {
        val gridLat = (userLocation.latitude / gridSize).toInt() * gridSize
        val gridLng = (userLocation.longitude / gridSize).toInt() * gridSize

        val gridKey = "$gridLat,$gridLng"

        // 이미 방문한 격자는 다시 색칠하지 않음
        if (visitedGrids.containsKey(gridKey)) return

        // 격자 색칠
        val gridPolygon = map.addPolygon(
            PolygonOptions()
                .add(
                    LatLng(gridLat, gridLng),
                    LatLng(gridLat, gridLng + gridSize),
                    LatLng(gridLat + gridSize, gridLng + gridSize),
                    LatLng(gridLat + gridSize, gridLng)
                )
                .strokeColor(0xFF00FF00.toInt()) // 테두리 색 (초록색)
                .strokeWidth(2f) // 선 두께
                .fillColor(0x5500FF00.toInt()) // 내부 색 (반투명 초록색)
        )
        visitedGrids[gridKey] = gridPolygon
    }
}
