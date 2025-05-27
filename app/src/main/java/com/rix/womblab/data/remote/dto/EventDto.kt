package com.rix.womblab.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class EventsResponseDto(
    val events: List<EventDto>,
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("rest_url")
    val restUrl: String
)

data class EventDto(
    val id: Int,
    @SerializedName("global_id")
    val globalId: String,
    val title: String,
    val description: String,
    val excerpt: String = "",
    val url: String,
    @SerializedName("rest_url")
    val restUrl: String,
    val image: EventImageDto?,
    @SerializedName("all_day")
    val allDay: Boolean,
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,
    @SerializedName("utc_start_date")
    val utcStartDate: String? = null,
    @SerializedName("utc_end_date")
    val utcEndDate: String? = null,
    @SerializedName("start_date_details")
    val startDateDetails: DateDetailsDto? = null,
    @SerializedName("end_date_details")
    val endDateDetails: DateDetailsDto? = null,
    @SerializedName("utc_start_date_details")
    val utcStartDateDetails: DateDetailsDto? = null,
    @SerializedName("utc_end_date_details")
    val utcEndDateDetails: DateDetailsDto? = null,
    val timezone: String,
    @SerializedName("timezone_abbr")
    val timezoneAbbr: String,
    val cost: String = "",
    @SerializedName("cost_details")
    val costDetails: CostDetailsDto,
    val website: String? = null,
    @SerializedName("show_map")
    val showMap: Boolean,
    @SerializedName("show_map_link")
    val showMapLink: Boolean,
    @SerializedName("hide_from_listings")
    val hideFromListings: Boolean,
    val sticky: Boolean,
    val featured: Boolean,
    val categories: List<EventCategoryDto> = emptyList(),
    val tags: List<EventTagDto> = emptyList(),
    // 🔧 FIXED: venue ora gestito con custom deserializer
    val venue: EventVenueDto? = null,
    val organizer: List<EventOrganizerDto> = emptyList(),
    @SerializedName("custom_fields")
    val customFields: Map<String, CustomFieldDto> = emptyMap(),
    val status: String = "publish"
)

data class EventImageDto(
    val url: String,
    val id: Int,
    val extension: String,
    val width: Int,
    val height: Int,
    val filesize: Int,
    val sizes: Map<String, ImageSizeDto> = emptyMap()
)

data class ImageSizeDto(
    val width: Int,
    val height: Int,
    @SerializedName("mime-type")
    val mimeType: String,
    val filesize: Int,
    val url: String
)

data class DateDetailsDto(
    val year: String = "2025",
    val month: String = "01",
    val day: String = "01",
    val hour: String = "00",
    val minutes: String = "00",
    val seconds: String = "00"
)

data class CostDetailsDto(
    @SerializedName("currency_symbol")
    val currencySymbol: String = "",
    @SerializedName("currency_code")
    val currencyCode: String = "",
    @SerializedName("currency_position")
    val currencyPosition: String = "",
    val values: List<String> = emptyList()
)

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
    @SerializedName("show_map")
    val showMap: Boolean,
    @SerializedName("show_map_link")
    val showMapLink: Boolean,
    @SerializedName("global_id")
    val globalId: String
)

data class EventOrganizerDto(
    val id: Int,
    val organizer: String,
    val slug: String,
    val phone: String? = null,
    val email: String? = null,
    @SerializedName("global_id")
    val globalId: String
)

data class EventCategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    @SerializedName("term_group")
    val termGroup: Int,
    @SerializedName("term_taxonomy_id")
    val termTaxonomyId: Int,
    val taxonomy: String,
    val description: String = "",
    val parent: Int,
    val count: Int,
    val filter: String,
    @SerializedName("term_order")
    val termOrder: String
)

data class EventTagDto(
    val id: Int,
    val name: String,
    val slug: String
)

data class CustomFieldDto(
    val label: String,
    val value: String
)

class VenueDeserializer : JsonDeserializer<EventVenueDto?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EventVenueDto? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonArray -> null
                json.isJsonObject -> {
                    val jsonObj = json.asJsonObject
                    EventVenueDto(
                        id = jsonObj.get("id")?.asInt ?: 0,
                        venue = jsonObj.get("venue")?.asString ?: "",
                        slug = jsonObj.get("slug")?.asString ?: "",
                        address = jsonObj.get("address")?.asString ?: "",
                        city = jsonObj.get("city")?.asString ?: "",
                        country = jsonObj.get("country")?.asString ?: "",
                        province = jsonObj.get("province")?.asString,
                        stateprovince = jsonObj.get("stateprovince")?.asString,
                        zip = jsonObj.get("zip")?.asString,
                        website = jsonObj.get("website")?.asString,
                        showMap = jsonObj.get("show_map")?.asBoolean ?: false,
                        showMapLink = jsonObj.get("show_map_link")?.asBoolean ?: false,
                        globalId = jsonObj.get("global_id")?.asString ?: ""
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}