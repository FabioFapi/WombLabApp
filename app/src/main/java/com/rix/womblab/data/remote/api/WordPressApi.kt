package com.rix.womblab.data.remote.api

import com.rix.womblab.data.remote.dto.EventDto
import com.rix.womblab.data.remote.dto.EventsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WordPressApi {

    companion object {
        const val BASE_URL = "https://www.womblab.com/"
        const val EVENTS_ENDPOINT = "wp-json/tribe/events/v1/events"
    }

    @GET(EVENTS_ENDPOINT)
    suspend fun getEvents(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("search") search: String? = null,
        @Query("categories") categories: String? = null,
        @Query("tags") tags: String? = null,
        @Query("featured") featured: Boolean? = null,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>

    @GET("$EVENTS_ENDPOINT/{id}")
    suspend fun getEventById(
        @Path("id") eventId: String
    ): Response<EventDto>

    @GET(EVENTS_ENDPOINT)
    suspend fun getUpcomingEvents(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("start_date") startDate: String,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>

    @GET(EVENTS_ENDPOINT)
    suspend fun getPastEvents(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("end_date") endDate: String,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>

    @GET(EVENTS_ENDPOINT)
    suspend fun getEventsByCategory(
        @Query("categories") categorySlug: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>

    @GET(EVENTS_ENDPOINT)
    suspend fun searchEvents(
        @Query("search") searchQuery: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>

    @GET(EVENTS_ENDPOINT)
    suspend fun getFeaturedEvents(
        @Query("featured") featured: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("status") status: String = "publish"
    ): Response<EventsResponseDto>
}