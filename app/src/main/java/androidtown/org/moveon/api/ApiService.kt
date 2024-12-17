package androidtown.org.moveon.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class UserStepInfo(
    val date: String,
    val steps: Int,
    val distance: Int,
    val run_time: String,
    val memo: String,
    val userId: Int,
    val hrate: Int
)
data class UserStepRequest(
    val userStepInfo: UserStepInfo,
)
interface ApiService {
    @POST("api/steps")
    fun uploadUserSteps(@Body request: UserStepRequest): Call<Void>
}
