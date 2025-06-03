# File: proguard-rules.pro

# Mantieni tutte le classi DTO
-keep class com.rix.womblab.data.remote.dto.** { *; }
-keep class com.rix.womblab.domain.model.** { *; }

# Retrofit e Gson
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Mantieni metodi dei DAO Room
-keep class com.rix.womblab.data.local.dao.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keepattributes *Annotation*

# Serialization
-keepattributes Signature
-keep class kotlin.Metadata { *; }
-keep class kotlinx.serialization.** { *; }