package com.rix.womblab.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventsResponseDto(
    val events: List<EventDto>,
    val total: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("rest_url")
    val restUrl: String
)

@Serializable
data class EventDto(
    val id: Int,
    @SerialName("global_id")
    val globalId: String,
    val title: String,
    val description: String,
    val excerpt: String = "",
    val url: String,
    @SerialName("rest_url")
    val restUrl: String,
    val image: EventImageDto?,
    @SerialName("all_day")
    val allDay: Boolean,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("utc_start_date")
    val utcStartDate: String? = null,
    @SerialName("utc_end_date")
    val utcEndDate: String? = null,
    @SerialName("start_date_details")
    val startDateDetails: DateDetailsDto? = null,
    @SerialName("end_date_details")
    val endDateDetails: DateDetailsDto? = null,
    @SerialName("utc_start_date_details")
    val utcStartDateDetails: DateDetailsDto? = null,
    @SerialName("utc_end_date_details")
    val utcEndDateDetails: DateDetailsDto? = null,
    val timezone: String,
    @SerialName("timezone_abbr")
    val timezoneAbbr: String,
    val cost: String = "",
    @SerialName("cost_details")
    val costDetails: CostDetailsDto,
    val website: String? = null,
    @SerialName("show_map")
    val showMap: Boolean,
    @SerialName("show_map_link")
    val showMapLink: Boolean,
    @SerialName("hide_from_listings")
    val hideFromListings: Boolean,
    val sticky: Boolean,
    val featured: Boolean,
    val categories: List<EventCategoryDto> = emptyList(),
    val tags: List<EventTagDto> = emptyList(),
    val venue: EventVenueDto?,
    val organizer: List<EventOrganizerDto> = emptyList(),
    @SerialName("custom_fields")
    val customFields: Map<String, CustomFieldDto> = emptyMap(),
    val status: String = "publish"
)

@Serializable
data class EventImageDto(
    val url: String,
    val id: Int,
    val extension: String,
    val width: Int,
    val height: Int,
    val filesize: Int,
    val sizes: Map<String, ImageSizeDto> = emptyMap()
)

@Serializable
data class ImageSizeDto(
    val width: Int,
    val height: Int,
    @SerialName("mime-type")
    val mimeType: String,
    val filesize: Int,
    val url: String
)

@Serializable
data class DateDetailsDto(
    val year: String = "2025",
    val month: String = "01",
    val day: String = "01",
    val hour: String = "00",
    val minutes: String = "00",
    val seconds: String = "00"
)

@Serializable
data class CostDetailsDto(
    @SerialName("currency_symbol")
    val currencySymbol: String = "",
    @SerialName("currency_code")
    val currencyCode: String = "",
    @SerialName("currency_position")
    val currencyPosition: String = "",
    val values: List<String> = emptyList()
)

@Serializable
data class EventVenueDto(
    val id: Int,
    val venue: String,
    val slug: String,
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val province: String? = null,
    val stateprovince: String? = null,
    val zip: String? = null,
    val website: String? = null,
    @SerialName("show_map")
    val showMap: Boolean,
    @SerialName("show_map_link")
    val showMapLink: Boolean,
    @SerialName("global_id")
    val globalId: String
)

@Serializable
data class EventOrganizerDto(
    val id: Int,
    val organizer: String,
    val slug: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("global_id")
    val globalId: String
)

@Serializable
data class EventCategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    @SerialName("term_group")
    val termGroup: Int,
    @SerialName("term_taxonomy_id")
    val termTaxonomyId: Int,
    val taxonomy: String,
    val description: String = "",
    val parent: Int,
    val count: Int,
    val filter: String,
    @SerialName("term_order")
    val termOrder: String
)

@Serializable
data class EventTagDto(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class CustomFieldDto(
    val label: String,
    val value: String
)