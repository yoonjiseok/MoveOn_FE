package androidtown.org.moveon

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SharedMapViewModel : ViewModel() {
    private val _cameraPosition = MutableLiveData<Pair<LatLng, Float>>()
    val cameraPosition: LiveData<Pair<LatLng, Float>> = _cameraPosition

    fun updateCameraPosition(latLng: LatLng, zoom: Float) {
        _cameraPosition.value = Pair(latLng, zoom)
    }
}
