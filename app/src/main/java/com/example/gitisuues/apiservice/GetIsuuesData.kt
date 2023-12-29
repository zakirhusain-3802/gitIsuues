package com.example.gitisuues.apiservice

import com.example.gitisuues.dataclass.issuesdataitem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GetIsuuesData {
    @GET("/repos/{owner}/{repo}/issues")
    fun getClosedIssues(@Path("owner") owner: String, @Path("repo") repo: String, @Query("state") status: String): Call<List<issuesdataitem>>
}
