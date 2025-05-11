/*package com.example.shuttle

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val supabase = createSupabaseClient(
    supabaseUrl = "https://yxahehaxkearsllzzcxp.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4YWhlaGF4a2VhcnNsbHp6Y3hwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMzMzQzMDAsImV4cCI6MjA1ODkxMDMwMH0.ZwmX86jazv_ynCzkpunHALd5zYjiMpkxNfdvg6mgdyQ"
) {
    install(Postgrest)
}

@Serializable
data class BusLocation(
    val id: String = UUID.randomUUID().toString(),
    val driver_id: String,
    val latitude: Double,
    val longitude: Double,
    val updated_at: String
)

object SupabaseRepository {
    suspend fun uploadLocation(driverId: String, lat: Double, lon: Double) {
        try {

            val location = BusLocation(
                driver_id = "3f6941e6-835e-4c62-9f4b-b49617cf312e",
                latitude = lat,
                longitude = lon,
                updated_at = Instant.now().toString()
            )


            withContext(Dispatchers.IO) {
                val result = supabase
                    .from("bus_locations")
                    .upsert(location) {
                        onConflict = "driver_id"
                    }

                LogHelper.print("Result: ${result.data}")
            }

            Log.d("SupabaseUpload", "Uploaded: $location")
        } catch (e: Exception) {
            Log.e("SupabaseUpload", "Failed: ${e.message}")
        }
    }
}*/

/*두번째
package com.example.shuttle

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val supabase = createSupabaseClient(
    supabaseUrl = "https://yxahehaxkearsllzzcxp.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4YWhlaGF4a2VhcnNsbHp6Y3hwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMzMzQzMDAsImV4cCI6MjA1ODkxMDMwMH0.ZwmX86jazv_ynCzkpunHALd5zYjiMpkxNfdvg6mgdyQ"
) {
    install(Postgrest)
}

@Serializable
data class BusLocation(
    val id: String = UUID.randomUUID().toString(),
    val driver_id: String,
    val latitude: Double,
    val longitude: Double,
    val updated_at: String
)

object SupabaseRepository {
    suspend fun uploadLocation(driverId: String, lat: Double, lon: Double) {
        try {

            val location = BusLocation(
                driver_id = "3f6941e6-835e-4c62-9f4b-b49617cf312e",
                latitude = lat,
                longitude = lon,
                updated_at = Instant.now().toString()
            )


            withContext(Dispatchers.IO) {
                val result = supabase
                    .from("bus_locations")
                    .upsert(location) {
                        onConflict = "driver_id"
                    }

                LogHelper.print("Result: ${result.data}")
            }

            Log.d("SupabaseUpload", "Uploaded: $location")
        } catch (e: Exception) {
            Log.e("SupabaseUpload", "Failed: ${e.message}")
        }
    }
} */

package com.example.test_driver2

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

val supabase = createSupabaseClient(
    supabaseUrl = "https://yxahehaxkearsllzzcxp.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4YWhlaGF4a2VhcnNsbHp6Y3hwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMzMzQzMDAsImV4cCI6MjA1ODkxMDMwMH0.ZwmX86jazv_ynCzkpunHALd5zYjiMpkxNfdvg6mgdyQ"
) {
    install(Postgrest)
}

@Serializable
data class Passenger(
    val id: Int,
    val name: String
)
@Serializable
data class TinoNum(
    val id: String,
    val passenger_num: Int,
    val reservation_num: Int
)

@Serializable
data class TinoLocation(
    val estimated_time: Int,
    val created_at: String,
    val driver_id: String,
    val speed: Int,
    val passengers: List<Passenger>,
    val lat: Double,
    val lng: Double,
    val id: Long = System.currentTimeMillis(),
    val driver_name: String,
    val status: String,
    val destination_name: String,
    val bus_plat_number: String,
    val departure_name: String
)

object SupabaseRepository {

    // 좌표 상수
    private val jeongwangStation = LatLng(37.3515, 126.7427)
    private val koreaTech = LatLng(37.3404, 126.7338)

    data class LatLng(val lat: Double, val lng: Double)

    // ETA 계산 함수
    private fun estimateArrivalTime(
        current: LatLng,
        destination: LatLng,
        speedMps: Double
    ): Int {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(destination.lat - current.lat)
        val dLon = Math.toRadians(destination.lng - current.lng)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(current.lat)) *
                cos(Math.toRadians(destination.lat)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        return if (speedMps > 0) (distance / speedMps).roundToInt() else 0
    }

    suspend fun updateLocation(
        driverId: String,
        lat: Double,
        lon: Double,
        speed: Int,
    ) {
        Log.d("PANGMOO", "UPDATE LOCATION BEGIN")
        supabase
            .from("tino")
            .update(
                {
                    TinoLocation::lat setTo lat
                    TinoLocation::lng setTo lon
                    TinoLocation::speed setTo speed
                    TinoLocation::created_at setTo Instant.now().toString()
                }
            ) {
                select()
                filter {
                    TinoLocation::driver_id eq driverId
                }
            }
            .decodeSingle<TinoLocation>()
            .let {
                Log.d("PANGMOO", "UPDATE LOCATION DONE: $it")
            }

    }

    suspend fun updateArrive(
        driverId: String,
        departureName: String,
        destinationName: String
    ) {
//        val converted = if (departureName == "정왕역")
//            destinationName to departureName
//        else
//            departureName to destinationName

        Log.d("PANGMOO", "UPDATE ARRIVE - ${departureName}/${destinationName}")

        supabase
            .from("tino")
            .update(
                {
                    TinoLocation::departure_name setTo departureName
                    TinoLocation::destination_name setTo destinationName
//                    TinoLocation::departure_name setTo converted.first
//                    TinoLocation::destination_name setTo converted.second
                }
            ) {
                filter {
                    TinoLocation::driver_id eq driverId
                }
            }

        Log.d("PANGMOO", "UPDATE ARRIVE DONE")
    }
    suspend fun fetchCurrentSpeed(driverId: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase
                    .from("tino")
                    .select {
                        TinoLocation::speed // 명시적으로 TinoLocation 데이터 클래스의 speed 프로퍼티를 선택
                        filter {
                            TinoLocation::driver_id eq driverId
                        }
                        limit(1)
                    }
                    .decodeSingle<TinoLocation>()
                response.speed
            } catch (e: Exception) {
                Log.e("SupabaseFetch", "Failed to fetch speed: ${e.message}")
                null
            }
        }
    }

    private val targetTinoNumId = "514369ad-f426-4555-8345-b34a2f76f682"

    suspend fun fetchPassengerNum(): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase
                    .from("tino_num")
                    .select {
                        TinoNum::passenger_num
                        filter {
                            TinoNum::id eq targetTinoNumId
                        }
                        limit(1)
                    }
                    .decodeSingle<TinoNum>()
                response.passenger_num
            } catch (e: Exception) {
                Log.e("SupabaseFetch", "Failed to fetch passenger_num: ${e.message}")
                null
            }
        }
    }

    suspend fun fetchDepartureAndDestination(driverId: String): TinoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase
                    .from("tino")
                    .select {
                        TinoLocation::departure_name
                        TinoLocation::destination_name
                        filter {
                            TinoLocation::driver_id eq driverId
                        }
                        limit(1) // 특정 driverId에 해당하는 최신 데이터 하나만 가져옴
                    }
                    .decodeSingle<TinoLocation>()
                response
            } catch (e: Exception) {
                Log.e("SupabaseFetch", "Failed to fetch departure and destination: ${e.message}")
                null
            }
        }
    }
    suspend fun bodydeparture(driverId: String): TinoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabase
                    .from("tino")
                    .select {
                        TinoLocation::departure_name
                        filter {
                            TinoLocation::driver_id eq driverId
                        }
                        limit(1) // 특정 driverId에 해당하는 최신 데이터 하나만 가져옴
                    }
                    .decodeSingle<TinoLocation>()
                response
            } catch (e: Exception) {
                Log.e("SupabaseFetch", "Failed to fetch departure and destination: ${e.message}")
                null
            }
        }
    }
    suspend fun bodydestination(driverId: String): TinoLocation? {
        return withContext(Dispatchers.IO) {
            try {
                val response= supabase
                    .from("tino")
                    .select {
                        TinoLocation::destination_name
                        filter {
                            TinoLocation::driver_id eq driverId
                        }
                        limit(1)
                    }
                    .decodeSingle<TinoLocation>()
                response
            } catch (e: Exception) {
                Log.e("SupabaseFetch", "Failed to fetch departure and destination: ${e.message}")
                null
            }
        }
    }




    suspend fun uploadLocation(
        driverId: String,
        driverName: String,
        lat: Double,
        lon: Double,
        speed: Int,
        status: String,
        destinationName: String,
        busPlateNumber: String,
        departureName: String
    ) {
//        return
        try {
            val current = LatLng(lat, lon)
            val speedMps = speed.toDouble() / 3.6

            // 목적지 좌표 설정
            val destination = when (destinationName) {
                "정왕역" -> jeongwangStation
                "한국공학대학교 정문" -> koreaTech
                else -> koreaTech // 기본값
            }

            // 상태에 따라 ETA 계산
            val estimatedTime = when (status) {
                "운행중", "탑승완료" -> estimateArrivalTime(
                    current,
                    destination,
                    speedMps
                ).coerceAtLeast(0)

                "도착" -> 0
                else -> 0
            }

            val passengersList = listOf(
                Passenger(id = 1, name = "메시"),
                Passenger(id = 2, name = "손흥민"),
                Passenger(id = 3, name = "음바페")
            )

            val location = TinoLocation(
                estimated_time = estimatedTime,
                created_at = Instant.now().toString(),
                driver_id = driverId,
                speed = speed,
                passengers = passengersList,
                lat = lat,
                lng = lon,
                driver_name = driverName,
                status = status,
                destination_name = destinationName,
                bus_plat_number = busPlateNumber,
                departure_name = departureName
            )

            withContext(Dispatchers.IO) {
                Log.d("PANGMOO", "GOGO")
                val result = supabase
                    .from("tino")
                    .upsert(location) {
                        onConflict = "driver_id"
                    }
                Log.d("PANGMOO", "DONE")

                Log.d("PANGMOO", "Uploaded: ${destinationName}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseUpload", "Failed: ${e.message}")
        }
    }
}




