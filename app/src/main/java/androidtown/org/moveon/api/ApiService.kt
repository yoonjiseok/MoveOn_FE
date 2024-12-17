package androidtown.org.moveon.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// 서버로 보낼 JSON 데이터 모델
data class StepRequest(
    val date: String,
    val steps: Int,
    val distance: Int,
    val run_time: String,
    val memo: String,
    val userId: Int,
    val hrate: Int
)

// 서버 응답 모델
data class StepResponse(
    val result: String,
    val message: String,
    val data: Any // 서버에서 반환할 추가 데이터 타입에 맞게 변경 가능
)

interface ApiService {
    @POST("api/steps")
    fun sendStepData(@Body stepRequest: StepRequest): Call<StepResponse>
}

