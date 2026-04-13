package com.example.mindaccess.Data.Repository

import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Model.GeoLocationCordModel
import com.example.mindaccess.Domain.Repository.CenterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Hashtable
import javax.inject.Inject
import kotlin.random.Random

class CenterRepositoryImpl @Inject constructor() : CenterRepository {
    override fun getCenters(): Flow<List<CenterModel>> = flow {
        delay(2000) // 2 second delay
        val centers = List(20) { index ->
            val lat = 40.7128 + (Random.nextDouble() - 0.5) * 0.1
            val lon = -74.0060 + (Random.nextDouble() - 0.5) * 0.1
            
            CenterModel(
                name = "Center Health Center IV Kra Municipality $index",
                description = "This is a description for center located in New York. This should test how the biges center dec looks like. Not probably the biggest but its ideal" +
                        "So here we are trying to convience peaople..just pray for us",
                location = GeoLocationCordModel(lat, lon),
                category = listOf("Medical", "Mental Health", "Community", "Youth").random(),
                workingDays = "Mon - Fri, 9AM - 5PM",
                contact = Hashtable<String, String>().apply {
                    put("phone", "555-010${index}")
                    put("email", "center${index + 1}@example.com")
                },
                services = listOf("Consultation", "Therapy", "Workshops", "Support Groups, Consultation, Therapy, Workshops, Support Groups","Support Groups, Consultation, Therapy, Workshops, Support Groups","Consultation", "Therapy", "Workshops",  ).shuffled().take(10)
            )
        }
        emit(centers)
    }
}
