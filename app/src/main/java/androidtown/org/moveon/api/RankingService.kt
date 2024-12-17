package androidtown.org.moveon.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class WeeklyRankingResponse(
    val result: String,
    val message: String,
    val data: List<RankItem>
)

data class RankItem(
    val userId: Int,
    val nickname: String,
    val profileImageURL: String,
    val currentPixelCount: Int,
    val rank: Int,
)
interface RankingService {
    @GET("api/ranking/user")
    fun getWeeklyRanking(@Query("lookup-date") date: String): Call<WeeklyRankingResponse>
}


